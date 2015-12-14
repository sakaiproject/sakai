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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.component.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.test.SakaiKernelTestBase;

/**
 *
 */
public class DynamicConfigurationTest extends SakaiKernelTestBase {
	private static Log log = LogFactory.getLog(DynamicConfigurationTest.class);

	private ServerConfigurationService serverConfigurationService;

	protected static final String CONFIG = "src/test/webapp/WEB-INF/components.xml";

	@BeforeClass
	public static void beforeClass() {
		try {
            log.debug("starting oneTimeSetup");
			oneTimeSetup("dynamic", CONFIG);
            log.debug("finished oneTimeSetup");
		} catch (Exception e) {
			log.warn(e);
		}
	}

	@Before
	public void setUp() throws Exception {
		serverConfigurationService = (ServerConfigurationService)getService(ServerConfigurationService.class.getName());
	}

	@Test
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

}
