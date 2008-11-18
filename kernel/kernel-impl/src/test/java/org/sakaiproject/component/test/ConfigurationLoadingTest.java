/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.component.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.test.SakaiKernelTestBase;

/**
 *
 */
public class ConfigurationLoadingTest extends SakaiKernelTestBase {
	private static Log log = LogFactory.getLog(ConfigurationLoadingTest.class);
	
	private ServerConfigurationService serverConfigurationService;

	protected static String CONFIG = "src/test/webapp/WEB-INF/components.xml";

	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(ConfigurationLoadingTest.class)) {
			protected void setUp() throws Exception {
				try {
					oneTimeSetup("filesystem", CONFIG);
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
	
	public void setUp() throws Exception {
		
		serverConfigurationService = (ServerConfigurationService)getService(ServerConfigurationService.class.getName());
	}
	

	public void testSakaiProperties() throws Exception {
		// Check that the test sakai-configuration.xml and sakai.properties files have been loaded.
		Assert.assertTrue(serverConfigurationService.getString("loadedTomcatSakaiProperties").equals("true"));
		Assert.assertTrue(serverConfigurationService.getString("gatewaySiteId").equals("!gateway"));
		ITestComponent testComponent = (ITestComponent)getService(ITestComponent.class.getName());
		Assert.assertTrue(testComponent.getOverrideString1().equals("nondefault"));
		Assert.assertTrue(testComponent.getPlaceholderString1().equals("nondefault"));
		if (log.isDebugEnabled()) log.debug("serverId=" + testComponent.getServerId());
		String testBean = (String)getService("org.sakaiproject.component.test.String");
		Assert.assertTrue(testBean.equals("local"));
		ITestProvider testProvider = (ITestProvider)getService(ITestProvider.class.getName());
		Assert.assertTrue(testProvider.getProviderName().equals("provider2"));
		
		Assert.assertTrue(testComponent.getListOverride1().size() == 3);
		Assert.assertTrue(testComponent.getListOverride1().get(0).equals("nondefault1"));
		Assert.assertTrue(testComponent.getMapOverride1().size() == 3);
		Assert.assertTrue(testComponent.getMapOverride1().get("key1").equals("nondefault1"));
		
		// Test for use of a local properties file other than sakai.properties.
		String[] stringArrayPlaceholder1 = testComponent.getStringArrayPlaceholder1();
		Assert.assertTrue(stringArrayPlaceholder1.length == 4);
		Assert.assertTrue(stringArrayPlaceholder1[0].equals("peculiar1"));
		
		// Test for promotion of certain Sakai properties to system properties.
		String uploadMax = System.getProperty("sakai.content.upload.max");
		Assert.assertTrue(uploadMax.equals("5"));
		
		// Test that an untouched component-defined alias came through.
		// <alias name="org.sakaiproject.component.test.ITestComponent" alias="testAliasRetention"/>
		Object aliasedObject = getService("testAliasRetention");
		Assert.assertTrue(aliasedObject instanceof ITestComponent);
	}
	
	/**
	 * As implemented (configured, actually), verifies that property placeholders are
	 * dereferenced recursively. I.e. the property retrieved references another property,
	 * which references another, and so on. Presumably simpler scenarios work implicitly.
	 */
	public void testGetStringDereferencesPlaceholderPropertiesForSimpleProperties() {
		assertEquals("Property value for \"stringWithNestedPlaceholders\" not dereferenced",
				"str1-str2-str3", serverConfigurationService.getString("stringWithNestedPlaceholders"));
	}
	
	/**
	 * Verifies that property placeholder dereferencing works as usual, even for properties
	 * having the special Sakai bean addressing syntax (propertyName@beanName). Note that this
	 * does not test bean property injection, only that these properties are given the
	 * same treatment as "simple" properties when accessed via 
	 * {@link ServerConfigurationService#getString(String)}.
	 */
	public void testGetStringDereferencesPlaceholderPropertiesForBeanAddressingProperties() {
		assertEquals("Property value for \"overrideString1@org.sakaiproject.component.test.ITestComponent2\" not dereferenced",
				"str1-str2-str3", serverConfigurationService.getString("overrideString1@org.sakaiproject.component.test.ITestComponent2"));
	}
	
	/**
	 * Verifies that property placeholders are dereferenced appropriately for special 
	 * "enumerated" properties.
	 * 
	 * An example of a set of "enumerated" properties without any placeholder values:
	 * 
	 * <p>
	 * <code>
	 * property.name.count=2
	 * property.name.1=val1
	 * property.name.2=val2
	 * </pre>
	 * </p>
	 * 
	 * <p>Requesting the value of <code>property.name</code> will result in an array containing
	 * the values of <code>property.name.1</code> and <code>property.name.2</code></p>
	 * 
	 * <p>As implemented (configured, actually), this test includes verifies that the property 
	 * count value is correctly dereferenced.</p> 
	 */
	public void testGetStringsDereferencesPlaceholderProperties() {
		String[] expected = new String[] { "str1", "str1-str2", "str1-str2-str3" };
		String[] actual = serverConfigurationService.getStrings("stringPlaceholderProps");
		assertTrue("Expected \"stringPlaceholderProps.*\" property values in an array matching " + Arrays.toString(expected) + ", but was " + Arrays.toString(actual), 
				Arrays.equals(expected, actual));
	}
	
	/**
	 * Verifies that property placeholder dereferencing works when Sakai "promotes" certain 
	 * properties into the system scope.
	 */
	public void testPropertyPromotionDereferencesPlaceholderProperties() {
		assertEquals("Property value for \"content.upload.dir\" not dereferenced or not placed into \"sakai.content.upload.dir\" system property", 
				"/str1-str2-str3", System.getProperty("sakai.content.upload.dir"));
	}
	
	/**
	 * When combined with {@link #testGetStringDereferencesPlaceholderPropertiesForSimpleProperties()}, 
	 * ensures symmetry between bean property placeholder dereferencing and getters on 
	 * {@link ServerConfigurationService}.
	 * 
	 * @see #testStringArrayBeanPropertyOverridingDereferencesPlaceholderProperties()
	 */
	public void testBeanStringPropertyPlaceholderDereferencingRecursivelyDereferencesPlaceholderProperties() {
		ITestComponent testComponent = (ITestComponent)getService(ITestComponent.class.getName() + "2");
		assertEquals("Bean property placeholder ${stringWithNestedPlaceholders} not dereferenced",
				"str1-str2-str3", testComponent.getPlaceholderString1());
	}
	
	/**
	 * When combined with {@link #testGetStringDereferencesPlaceholderPropertiesForBeanAddressingProperties()}, 
	 * ensures symmetry between bean property override behaviors and getters on 
	 * {@link ServerConfigurationService}.
	 */
	public void testBeanStringPropertyOverridingDereferencesPlaceholderProperties() {
		ITestComponent testComponent = (ITestComponent)getService(ITestComponent.class.getName() + "2");
		assertEquals("Property value not dereferenced when overriding bean string property \"overrideString1\"",
				"str1-str2-str3", testComponent.getOverrideString1());
	}
	
	/**
	 * Verifies that property dereferencing works as usual even for the special Sakai
	 * Spring bean list property override syntax (propertyName[index]@beanName=val)
	 */
	@SuppressWarnings("serial")
	public void testBeanListPropertyOverridingDereferencesPlaceholderProperties() {
		ITestComponent testComponent = (ITestComponent)getService(ITestComponent.class.getName() + "2");
		List<String> expected = new ArrayList<String>() {{
			add("str1");
			add("str1-str2");
			add("str1-str2-str3");
		}};
		assertEquals("Property values not dereferenced when overriding bean list property", 
				expected, testComponent.getListOverride1());
	}
	
	/**
	 * Verifies that property dereferencing works as usual even for String-encoded arrays
	 * set by properties having the special Sakai Spring bean addressing syntax
	 * (propertyName@beanName=val,val,val)
	 */
	public void testStringArrayBeanPropertyOverridingDereferencesPlaceholderProperties() {
		ITestComponent testComponent = (ITestComponent)getService(ITestComponent.class.getName() + "2");
		String[] expected = new String[] { "str1", "str1-str2", "str1-str2-str3" };
		String[] actual = trim(testComponent.getStringArrayPlaceholder1());
		assertTrue("Property values not dereferenced when overriding bean string array property as CSV list. Expected " + Arrays.toString(expected) + ", but was " + Arrays.toString(actual),
				Arrays.equals(expected, actual));
	}
	
	public void testGetRawPropertyDoesNotDereferencePlaceholderPropertiesForSimpleProperties() {
		assertEquals("Property value for \"stringWithNestedPlaceholders\" inappropriately dereferenced",
				"${stringWithPlaceholder}-str3", serverConfigurationService.getRawProperty("stringWithNestedPlaceholders"));
	}
	
	// highly unlikely to vary by property key syntax
	public void testGetStringAndGetRawPropertyReturnSameValueForUndefinedProperty() {
		assertEquals("Mismatched representations of undefined properties",
				serverConfigurationService.getString("this.property.is.not.defined"),
				serverConfigurationService.getRawProperty("this.propertie.is.not.defined"));
	}
	
	public void testGetRawPropertyDoesNotDereferencePlaceholderPropertiesForBeanAddressingProperties() {
		assertEquals("Property value for \"overrideString1@org.sakaiproject.component.test.ITestComponent2\" inappropriately dereferenced",
				"${stringWithNestedPlaceholders}", serverConfigurationService.getRawProperty("overrideString1@org.sakaiproject.component.test.ITestComponent2"));
	}

	private String[] trim(String[] strArray) {
		if ( strArray == null ) return null;
		for (int p = 0; p < strArray.length; p++ ) {
			if ( strArray[p] == null ) continue;
			strArray[p] = strArray[p].trim();
		}
		return strArray;
	}
}
