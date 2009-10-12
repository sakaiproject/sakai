/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.test;

import static org.sakaiproject.test.ComponentContainerEmulator.setTestSakaiHome;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Allows for Spring dependency injection from Sakai while using an emulated component
 * container (i.e., without running Tomcat or another web server).
 * 
 * As with the Spring superclass, AbstractDependencyInjectionSpringContextTests,
 * you can set up your own Spring application context by returning a list
 * of resource locations from "getConfigLocations()", The difference is that
 * your application context will have Sakai's component-level context as its parent.
 * This models the bean visibility you get with a Sakai web application (although
 * the classloading still won't be completely realistic).
 * 
 * If you run more than one integration test and you need a clean start between
 * tests to avoid static debris in the component system, make sure to include
 * a call to "setDirty()" in a "tearDown()" or "oneTimeTearDown()" method. 
 */
public class SakaiDependencyInjectionTests extends AbstractDependencyInjectionSpringContextTests {
	private static final Log log = LogFactory.getLog(SakaiDependencyInjectionTests.class);
	
	@Override
	protected ConfigurableApplicationContext createApplicationContext(String[] locations) {
		if (log.isDebugEnabled()) log.debug("createApplicationContext locations=" + Arrays.asList(locations));
		ComponentContainerEmulator.startComponentManagerForTest();		
		ConfigurableApplicationContext componentContext = (ConfigurableApplicationContext)ComponentContainerEmulator.getContainerApplicationContext();

		// WARNING: Copied from the superclass! The only change is to add a 
		// parent application context to the application context constructor.
		GenericApplicationContext context = new GenericApplicationContext(componentContext);
		customizeBeanFactory(context.getDefaultListableBeanFactory());
		new XmlBeanDefinitionReader(context).loadBeanDefinitions(locations);
		context.refresh();
		
		return context;
	}

	@Override
	protected void setDirty() {
		super.setDirty();
		
		// The above call will have closed this test's application context, but
		// make sure the component manager context is closed as well.
		ComponentContainerEmulator.stopComponentManager();
	}

	protected static final void setSakaiHome(String path) {
		setTestSakaiHome(path);
	}

}
