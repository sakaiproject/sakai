/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.test;

import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.sakaiproject.component.cover.TestComponentManagerContainer;

/**
 * Base class for kernel integration tests, provides methods to bring up the Component manager
 * loaded with the kernel component.
 * @author ieb
 *
 */
public class SakaiKernelTestBase {

	/**
	 * The configuration path of the components file for the kernel component
	 */
	private static String CONFIG = "../kernel-impl/src/main/webapp/WEB-INF/components.xml";

	/**
	 * The test component manager container
	 */
	protected static TestComponentManagerContainer testComponentManagerContainer;

	/**
	 * get the a bean from the component manager
	 * @param beanId
	 * @return
	 */
	protected static Object getService(String beanId) {
		return testComponentManagerContainer.getService(beanId);
	}

	/**
	 * Get a service bean from the component manager by its unique interface.
	 * @param class interface
	 * @return service or null if not found
	 */
	protected static <T> T getService(Class<T> clazz) {
		return (T)testComponentManagerContainer.getService(clazz.getName());
	}

	/**
	 * Perform a one time setup on the Component Manager, with possible additional components.
	 * @param sakaiHomeResources if not null, a subdirectory of test resources which
	 *   contains test-specific component configuration files such as "sakai.properties"
	 * @param additional if not null, the string is appended to the standard set of 
	 *   component descriptions. The string should point to a list of "components.xml"
	 *   file paths separated by semicolons.
	 * @param properties additional sakai properties to use. The gotcha is that at the moment
	 *                   it still uses the spring format of beanID@property rather than the Sakai
	 *                   format of property@beanID.
	 * @throws IOException
	 */
	protected static void oneTimeSetup(String sakaiHomeResources, String additional, Properties properties) throws IOException {
		//Set this property for testing
		System.setProperty("sakai.test", "src/test/resources/");
		if (sakaiHomeResources != null) {
			// TODO Better to store existing sakai.home setting for restoration in the tear down? 
			TestComponentManagerContainer.setSakaiHome("src/test/resources/" + sakaiHomeResources);
		}
		
		if (additional != null) {
			testComponentManagerContainer = new TestComponentManagerContainer(
					CONFIG + ";" + additional, properties);
		} else {
			testComponentManagerContainer = new TestComponentManagerContainer(
					CONFIG, properties);
		}
	}
	
	/**
	 * Perform any needed one time setup before starting the Component Manager.
	 */
	protected static void oneTimeSetup() throws IOException {
		oneTimeSetup(null, null, null);
	}
	
	/**
	 * Perform a one time setup on the Component Manager, with test-specific system
	 * configuration.
	 * @param sakaiHomeResources if not null, a subdirectory of test resources which
	 *   contains test-specific component configuration files such as "sakai.properties"
	 * @throws IOException
	 */
	protected static void oneTimeSetup(String sakaiHomeResources) throws IOException {
		oneTimeSetup(sakaiHomeResources, null, null);
	}

	/**
	 * Perform any needed one time setup before starting the Component Manager.
	 */
	protected static void oneTimeSetup(String sakaiHomeResources, String additional) throws IOException {
		oneTimeSetup(sakaiHomeResources, additional, null);
	}

	/**
	 * Pull the component manager down. This is done quietly so as not to alarm users.
	 */
	@AfterClass
	public static void oneTimeTearDown() {
		if (testComponentManagerContainer != null) {
			testComponentManagerContainer.getComponentManager().close();
		}
	}

}
