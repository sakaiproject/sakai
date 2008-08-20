/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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

package org.sakaiproject.test;

import java.io.IOException;

import org.sakaiproject.component.cover.TestComponentManagerContainer;
import org.sakaiproject.util.NoisierDefaultListableBeanFactory;

import junit.framework.TestCase;

/**
 * Base class for kernel integration tests, provides methods to bring up the Component manager
 * loaded with the kernel component.
 * @author ieb
 *
 */
public class SakaiKernelTestBase extends TestCase {

	/**
	 * The configuration path of the components file for the kernel component
	 */
	private static String CONFIG = "../kernel-component/src/main/webapp/WEB-INF/components.xml";

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
	 * Perform a one time setup on the Compionent manager, with possible additional components.
	 * @param additional if not null the string is appended to the sandard set of component description
	 * @throws IOException
	 */
	protected static void oneTimeSetup(String additional) throws IOException {
		if (additional != null) {
			testComponentManagerContainer = new TestComponentManagerContainer(
					CONFIG + ";" + additional);
		} else {
			testComponentManagerContainer = new TestComponentManagerContainer(
					CONFIG);

		}
	}

	/**
	 * Pull the component manager down. This is done quietly so as not to alarm users.
	 */
	protected static void oneTimeTearDown() {
		NoisierDefaultListableBeanFactory.noisyClose = false;
		testComponentManagerContainer.getComponentManager().close();
		NoisierDefaultListableBeanFactory.noisyClose = true;
	}

}
