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

package org.sakaiproject.component.cover;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.sakaiproject.component.impl.SpringCompMgr;

/**
 * A container for a Test Component Manager that can be configured with one of more components.
 *
 */
@Slf4j
public class TestComponentManagerContainer {
	/**
	 * The current component manager
	 */
	private SpringCompMgr componentManager;


	public TestComponentManagerContainer(String configPaths) throws IOException {
		this(configPaths, null);
	}

	/**
	 * create a component manager based on a list of component.xml
	 * @param configPaths a ';' seperated list of xml bean config files
	 * @throws IOException
	 */
	public TestComponentManagerContainer(String configPaths, Properties props)  throws IOException {
		// we assume that all the jars are in the same classloader, so this will
		// not check for
		// incorrect bindings and will not fully replicate the tomcat
		// experience, but is an easier environment
		// to work within for kernel testing.
		// For a more complex structure we could use the kernel poms in the repo
		// to generate the dep list.
		if ( ComponentManager.m_componentManager != null ) {
			log.info("Closing existing Component Manager ");
			/*			
 			try {
				ComponentManager.m_componentManager.close();
			} catch ( Throwable t ) {
				log.warn("Close Failed with message, safe to ignore "+t.getMessage());
			}
			*/
			log.info("Closing Complete ");
			ComponentManager.m_componentManager = null;
		}
		
		log.info("Starting Component Manager with ["+configPaths+"]");
		ComponentManager.setLateRefresh(true);

		componentManager = (SpringCompMgr) ComponentManager.getInstance();
		ConfigurableApplicationContext ac = componentManager.getApplicationContext();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		// Supply any additional configuration.
		if (props != null) {
			PropertyOverrideConfigurer beanFactoryPostProcessor = new PropertyOverrideConfigurer();
			beanFactoryPostProcessor.setBeanNameSeparator("@");
			beanFactoryPostProcessor.setProperties(props);
			ac.addBeanFactoryPostProcessor(beanFactoryPostProcessor);
		}

		// we could take the kernel bootstrap from from the classpath in future
		// rather than from
		// the filesystem

		List<Resource> config = new ArrayList<Resource>();
		String[] configPath = configPaths.split(";");
		for ( String p : configPath) {
			File xml = new File(p);
			config.add(new FileSystemResource(xml.getCanonicalPath()));
		}
		loadComponent(ac, config, classLoader);
		
		ac.refresh();

		// SAK-20908 - band-aid for TLM sync issues causing tests to fail
		// This sleep shouldn't be needed but it seems these tests are starting before ThreadLocalManager has finished its startup.
        try {
            Thread.sleep(500); // 1/2 second
            log.debug("Finished starting the component manager");
        } catch (InterruptedException e) {
            log.error("Component manager startup interrupted...");
        }
	}

	/**
	 * Load the application context using a single classloader
	 * @param ac The spring application context
	 * @param config a list of configurations represented as List of resources
	 * @param loader the classloader to use
	 */
	public void loadComponent(ConfigurableApplicationContext ac,
			List<Resource> config, ClassLoader loader) {
		ClassLoader current = Thread.currentThread().getContextClassLoader();

		Thread.currentThread().setContextClassLoader(loader);

		try {

			// make a reader
			XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(
					(BeanDefinitionRegistry) ac.getBeanFactory());

			// In Spring 2, classes aren't loaded during bean parsing unless
			// this
			// classloader property is set.
			reader.setBeanClassLoader(loader);

			reader.loadBeanDefinitions(config.toArray(new Resource[0]));
		} catch (Throwable t) {
			log.warn("loadComponentPackage: exception loading: " + config
					+ " : " + t, t);
		} finally {
			// restore the context loader
			Thread.currentThread().setContextClassLoader(current);
		}

	}

	/**
	 * get the current component manager
	 * @return
	 */
	public org.sakaiproject.component.api.ComponentManager getComponentManager() {
		return componentManager;
	}

	/**
	 * Convenience method to create a somewhat unique site id for testing.  Useful
	 * in tests that need to create a site to run tests upon.
	 * 
	 * @return A string suitable for using as a site id.
	 */
	protected String generateSiteId() {
		return "site-" + getClass().getName() + "-" + Math.floor(Math.random()*100000);
	}
	
	/**
	 * Returns a dynamic proxy for a service interface.  Useful for testing with
	 * customized service implementations without needing to write custom stubs.
	 * 
	 * @param clazz The service interface class
	 * @param handler The invocation handler that defines how the dynamic proxy should behave
	 * 
	 * @return The dynamic proxy to use as a collaborator
	 */
	public static final Object getServiceProxy(Class clazz, InvocationHandler handler) {
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {clazz}, handler);
	}
	
	public static void setSakaiHome(String sakaiHome) {
		System.setProperty("sakai.home", sakaiHome);
	}

	/**
	 * Convenience method to get a service bean from the Sakai component manager.
	 * 
	 * @param beanId The id of the service
	 * 
	 * @return The service, or null if the ID is not registered
	 */
	public Object getService(String beanId) {
		try {			
			return componentManager.get(beanId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

}
