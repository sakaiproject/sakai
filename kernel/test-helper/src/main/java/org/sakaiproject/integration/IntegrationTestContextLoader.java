/**
 * Copyright (c) 2003-2020 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.integration;

import java.io.IOException;

import org.sakaiproject.component.cover.TestComponentManagerContainer;
import org.sakaiproject.component.impl.SpringCompMgr;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebMergedContextConfiguration;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * ContextLoader for integration tests that loads all of the kernel components
 * and wires up the parent application context.
 *
 */
public class IntegrationTestContextLoader extends AnnotationConfigWebContextLoader {

	/**
	 * Path within the test-helper JAR to the parent components.xml file, imported
	 * from kernel-impl.
	 */
	public static final String KERNEL_COMPONENTS = "sakai-test-helper/components/components.xml";

	/**
	 * Path within the test-helper JAR to the sakai.test properties directory.
	 */
	public static final String SAKAI_TEST_PATH = "sakai-test-helper/sakai.test/";

	private static final String SAKAI_TEST = "sakai.test";

	@Override
	protected void customizeContext(GenericWebApplicationContext context,
			WebMergedContextConfiguration webMergedConfig) {
		setSystemProperties();
		startComponentManager(context);
	}

	/**
	 * Start up a test ComponentManager and wire up the Spring ApplicationContext.
	 */
	protected void startComponentManager(GenericWebApplicationContext context) {
		try {
			TestComponentManagerContainer container = new TestComponentManagerContainer(componentsPath());
			SpringCompMgr cm = (SpringCompMgr) container.getComponentManager();
			context.setParent(cm.getApplicationContext());
		} catch (IOException e) {
			throw new RuntimeException("Could not load kernel components while loading context!", e);
		}
	}

	/** Set system properties that the ComponentManager needs to start. */
	protected void setSystemProperties() {
		System.setProperty(SAKAI_TEST, sakaiTestPath());
	}

	/** Absolute path to components.xml that the ComponentManager can use. */
	private String componentsPath() {
		return pathInJar(KERNEL_COMPONENTS);
	}

	/**
	 * Absolute path for the "sakai.test" system property.
	 */
	private String sakaiTestPath() {
		return System.getProperty(SAKAI_TEST, pathInJar(SAKAI_TEST_PATH));
	}

	/** Resolve a classpath-relative path to an absolute path for use outside. */
	private String pathInJar(String path) {
		try {
			ClassPathResource resource = new ClassPathResource(path, getClass().getClassLoader());
			return resource.getFile().getAbsolutePath();
		} catch (IOException e) {
			throw new RuntimeException("Could not resolve '" + path + "' from the test-helper classpath");
		}
	}
}
