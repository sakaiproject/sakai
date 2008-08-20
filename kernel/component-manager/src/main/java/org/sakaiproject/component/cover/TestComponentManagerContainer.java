package org.sakaiproject.component.cover;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.impl.SpringCompMgr;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class TestComponentManagerContainer {
	
	private static final Log log = LogFactory.getLog(TestComponentManagerContainer.class);
	private SpringCompMgr componentManager;

	public TestComponentManagerContainer(String configPaths)  throws IOException {
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

		 componentManager = (SpringCompMgr) ComponentManager
				.getInstance();
		ConfigurableApplicationContext ac = componentManager
				.getApplicationContext();
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

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

		log.debug("Finished starting the component manager");
		// TODO Auto-generated constructor stub
	}

	public static void loadComponent(ConfigurableApplicationContext ac,
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
			log.error(e);
			return null;
		}
	}

	public static void addComponent(String config) {
		// TODO Auto-generated method stub
		
	}

}
