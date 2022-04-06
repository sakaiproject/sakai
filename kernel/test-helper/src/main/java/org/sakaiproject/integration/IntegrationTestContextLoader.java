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

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.impl.SpringCompMgr;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebMergedContextConfiguration;
import org.springframework.web.context.support.GenericWebApplicationContext;

import lombok.extern.slf4j.Slf4j;

/**
 * ContextLoader for integration tests that loads all of the kernel components
 * and wires up the parent application context.
 *
 */
@Slf4j
public class IntegrationTestContextLoader extends AnnotationConfigWebContextLoader {
	/** Our test context's component manager */
	private SpringCompMgr componentManager;

	/** The configuration (like sakai-configuration.xml) for this test suite */
	private Resource testConfig;

	/** The set of components to load for this test suite (like components.xml) */
	private Resource components;

	/** Basic constructor; sets standard config and components from the kernel. */
	public IntegrationTestContextLoader() {
		super();
		testConfig = new ClassPathResource("org/sakaiproject/config/test-configuration.xml");
		components = new ClassPathResource("org/sakaiproject/kernel/components.xml");
	}

	@Override
	protected void customizeContext(GenericWebApplicationContext context,
			WebMergedContextConfiguration webMergedConfig) {
		addListeners(context);
		startComponentManager(context);
	}

	/**
	 * Set up any context lifecycle event handlers.
	 *
	 * We start the component manager, but we need to listen to Spring to know when
	 * to clean up and shut down.
	 */
	protected void addListeners(GenericWebApplicationContext context) {
		context.addApplicationListener(closeListener());
	}

	/**
	 * Start up a test ComponentManager and wire up the Spring ApplicationContext.
	 *
	 * Ensure that the ComponentManager cover gets our instance immediately so
	 * startup can complete.
	 */
	protected void startComponentManager(GenericWebApplicationContext context) {
		componentManager = new SpringCompMgr(context, testConfig, components, ComponentManager.getBinding());
	}

	protected ApplicationListener<ContextClosedEvent> closeListener() {
		return new ApplicationListener<ContextClosedEvent>() {
			public void onApplicationEvent(ContextClosedEvent event) {
				log.info("Receieved context close event, closing Component Manager: " + event.toString());
				componentManager.close();
			}
		};
	}
}
