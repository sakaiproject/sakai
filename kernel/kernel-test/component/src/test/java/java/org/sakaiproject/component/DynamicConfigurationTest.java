/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2007 The Regents of the University of California
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.component;

import java.net.URL;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.test.ITestComponent;
import org.sakaiproject.test.SakaiTestBase;

/**
 *
 */
public class DynamicConfigurationTest extends SakaiTestBase {
	private static Log log = LogFactory.getLog(DynamicConfigurationTest.class);

	private ServerConfigurationService serverConfigurationService;

	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(DynamicConfigurationTest.class)) {
			protected void setUp() throws Exception {
				initializeSakaiHome();
				try {
					oneTimeSetup();
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

	public void testDynamicProperties() throws Exception {
		// Test for override of "sakai.properties" value by DB.
		String dynamicValue1 = serverConfigurationService.getString("dynamicKey1");
		Assert.assertTrue("initialDynamicValue1".equals(dynamicValue1));

		// Test for override of component property.
		ITestComponent testComponent = (ITestComponent)getService(ITestComponent.class.getName());
		Assert.assertTrue(testComponent.getOverrideString1().equals("dynamic"));

		// Test for dynamic property setting and retrieval using AOP?
		// Not really needed for testing SAK-8315 changes, but could
		// be interesting for developers to see.
	}

	public static void initializeSakaiHome() {
		URL propertiesUrl = ConfigurationLoadingTest.class.getClassLoader().getResource("dynamic/sakai.properties");
		if (log.isDebugEnabled()) log.debug("propertiesUrl=" + propertiesUrl);
		if (propertiesUrl != null) {
			String propertiesFileName = propertiesUrl.getFile();
			String sakaiHomeDir = propertiesFileName.substring(0, propertiesFileName.lastIndexOf("sakai.properties") - 1);
			System.setProperty("test.sakai.home", sakaiHomeDir);
		}
	}

}
