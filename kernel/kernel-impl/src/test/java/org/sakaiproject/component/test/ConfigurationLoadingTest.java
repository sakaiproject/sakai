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
}
