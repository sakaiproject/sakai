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

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Emulate the Sakai component environment set up by a running Tomcat container. 
 */
public class ComponentContainerEmulator {
	private static final Log log = LogFactory.getLog(ComponentContainerEmulator.class);
	private static Object componentManager;
	
	/**
	 * Configures the emulated component container to run integration tests.
	 */
	public static void startComponentManagerForTest() {
		startComponentManager(findTestTomcatHome(), findTestSakaiHome());
	}
	
	/**
	 * Configures the emulated component container to run against the default deployment environment.
	 */
	public static void startComponentManager() {
		startComponentManager(findTomcatHome(), null);
	}
	
	public static void startComponentManager(String tomcatHome, String sakaiHome) {
		if (log.isDebugEnabled()) log.debug("Starting the component manager; sakaiHome=" + sakaiHome + ", tomcatHome=" + tomcatHome);
		if (isStarted()) {
			if (log.isInfoEnabled()) log.info("Component manager already exists, so not starting after all");
			return;
		}

		// Normalize file path.
		char lastChar = tomcatHome.charAt(tomcatHome.length() - 1);
		if ((lastChar != '/') && (lastChar != '\\')) {
			tomcatHome += "/";
		}
		
		// Set the system properties needed by the sakai component manager
		if ((sakaiHome != null) && (sakaiHome.length() > 0)) {
			System.setProperty("sakai.home", sakaiHome);
		}
		System.setProperty("sakai.components.root", tomcatHome + "components/");
		
		// Add the sakai jars to the current classpath.  Note:  We are limited to using the sun jvm now
		URL[] sakaiUrls = getJarUrls(new String[] {tomcatHome + "common/endorsed/",
				tomcatHome + "common/lib/", tomcatHome + "shared/lib/"});
		URLClassLoader appClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
		try {
			Method addMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] {URL.class});
			addMethod.setAccessible(true);
			for(int i=0; i<sakaiUrls.length; i++) {
				addMethod.invoke(appClassLoader, new Object[] {sakaiUrls[i]});
			}
			
			Class<?> clazz = Class.forName("org.sakaiproject.component.cover.ComponentManager");
			componentManager = clazz.getDeclaredMethod("getInstance", (Class[])null).invoke((Object[])null, (Object[])null);
		} catch (Exception e) {
			// Wrap as runtime exception, since it's unlikely the caller will want to do
			// anything but die.
			if (e instanceof RuntimeException) {
				throw (RuntimeException)e;
			} else {
				throw new RuntimeException(e);
			}
		}

		if (log.isDebugEnabled()) log.debug("Finished starting the component manager");
	}
	
	public static void stopComponentManager() {
		if (log.isDebugEnabled()) log.debug("Starting the component manager");
		if(componentManager != null) {
			try {
				Method closeMethod = componentManager.getClass().getMethod("close", new Class[0]);
				closeMethod.invoke(componentManager, new Object[0]);
				componentManager = null;
			} catch (Exception e) {
				log.error(e);
			}
		} else {
			if (log.isInfoEnabled()) log.info("Component manager already stopped");				
		}
	}
	
	public static boolean isStarted() {
		return (componentManager != null);
	}
	
	/**
	 * @return a Spring ApplicationContext which can be specified as the parent
	 * for a client-loaded application context. It is NOT guaranteed to be useful
	 * for any other purpose.
	 */
	public static Object getContainerApplicationContext() {
		Object applicationContext = null;
		if (componentManager != null) {
			try {
				Method getContextMethod = componentManager.getClass().getMethod("getApplicationContext", new Class[0]);
				applicationContext = getContextMethod.invoke(componentManager, new Object[0]);
			} catch (Exception e) {
				log.error(e);
			}
		}
		return applicationContext;
	}
	
	/**
	 * Convenience method to get a service bean from the Sakai component manager.
	 * 
	 * @param beanId The id of the service
	 * 
	 * @return The service, or null if the ID is not registered
	 */
	public static final Object getService(String beanId) {
		try {
			Method getMethod = componentManager.getClass().getMethod("get", new Class[] {String.class});
			return getMethod.invoke(componentManager, new Object[] {beanId});
		} catch (Exception e) {
			log.error(e, e);
			return null;
		}
	}
	
	/**
	 * Convenience method to get a singleton service bean whose ID is the same
	 * as the input interface.
	 * 
	 * @param clazz the interface of the singleton service
	 * @return the implementing service
	 */
	public static final <T> T getService(Class<T> clazz) {
		String beanId = clazz.getName();
		return (T) getService(beanId);
	}
	
	/**
	 * Work around Maven's inability to simply pass through selected system properties.
	 * By default, Surefire tests inherit no Java properties from the Maven process itself.
	 * Only property names explicitly set in the plug-in's "systemProperties" configuration
	 * will be available. So, for example, to pass the "sakai.home" property, you'd need
	 * something like this:
	 * 
	 * <pre>
	 * &lt;plugin&gt;
	 *   &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;
	 *   &lt;artifactId&gt;maven-surefire-plugin&lt;/artifactId&gt;
	 *   &lt;configuration&gt;
	 *     &lt;systemProperties&gt;
	 *       &lt;property&gt;
	 *         &lt;name&gt;sakai.home&lt;/name&gt;
	 *         &lt;value&gt;${sakai.home}&lt;/value&gt;
	 *       &lt;/property&gt;
	 *     &lt;/systemProperties&gt;
	 *   &lt;/configuration&gt;
	 * &lt;/plugin&gt;
	 * </pre>
	 * 
	 * This doesn't work for optional Java properties, however. In the example above,
	 * if "sakai.home" is undefined, the Surefire plug-in will pass through a
	 * bogus string value of "${sakai.home}" rather than null. This utiltiy method
	 * checks for that condition.
	 * 
	 * @return The Java system property value, or null if the value appears to
	 * be a Maven property reference itself.
	 */
	public static final String getPassthroughSystemProperty(String propertyName) {
		String value = System.getProperty(propertyName);
		if ((value != null) && value.matches("\\$\\{.+\\}")) {
			value = null;
		}
		return value;
	}
	
	/**
	 * Point test.sakai.home at a directory in the test's resources area (typically
	 * "target/test-classes" for a Maven build) so that test-specific sakai.properties
	 * can be loaded.
	 * 
	 * @param pathPrefix the directory holding "sakai.properties" or "sakai-configuration.xml".
	 * 		an empty string indicates that the top of the classes directory should be used
	 */
	public static final void setTestSakaiHome(String pathPrefix) {
		URL sakaiHomeUrl = ComponentContainerEmulator.class.getClassLoader().getResource(pathPrefix);
		if (log.isDebugEnabled()) log.debug("sakaiHomeUrl=" + sakaiHomeUrl);
		if (sakaiHomeUrl != null) {
			System.setProperty("test.sakai.home", sakaiHomeUrl.getFile());
		}
	}

	/**
	 * Looks for a Tomcat home.
	 * 
	 * @return the value of the Java system property "maven.tomcat.home" or of the CATALINA_HOME
	 *   environment variable
	 * @throws Exception
	 */
	public static final String findTomcatHome() {
		String tomcatHome = getPassthroughSystemProperty("maven.tomcat.home");
		if ( tomcatHome != null && tomcatHome.length() > 0 ) {
			if (log.isDebugEnabled()) log.debug("Using maven.tomcat.home: " + tomcatHome);
		} else {
			// For the sake of Eclipse, provide a non-Maven-ish approach.
			tomcatHome = System.getenv("CATALINA_HOME");
			if (log.isDebugEnabled()) log.debug("Using CATALINA_HOME: " + tomcatHome);
		}
		return tomcatHome;
	}
	
	/**
	 * For backwards compatibility, checks for the Java system property
	 * "test.tomcat.home" or environment variable "TEST_CATALINA_HOME".
	 * If either is set, use it. If neither is set, go ahead and use the
	 * standard Tomcat home logic (which is the preferred approach).
	 * 
	 * @return
	 */
	public static final String findTestTomcatHome() {
		String tomcatHome = getPassthroughSystemProperty("test.tomcat.home");
		if ((tomcatHome != null) && (tomcatHome.length() > 0)) {
			if (log.isDebugEnabled()) log.debug("Using test.tomcat.home: " + tomcatHome);
		} else {
			// For the sake of Eclipse, provide a non-Maven-ish approach.
			tomcatHome = System.getenv("TEST_CATALINA_HOME");
			if ((tomcatHome != null) && (tomcatHome.length() > 0)) {
				if (log.isDebugEnabled()) log.debug("Using TEST_CATALINA_HOME: " + tomcatHome);
			} else {
				tomcatHome = findTomcatHome();
			}
		}
		return tomcatHome;
	}
	
	/**
	 * @return the value of the "test.sakai.home" Java system property, the value
	 * of the TEST_SAKAI_HOME environment variable, or null if neither is set.
	 */
	public static final String findTestSakaiHome() {
		String sakaiHome = getPassthroughSystemProperty("test.sakai.home");	// Can be null
		if ((sakaiHome != null) && (sakaiHome.length() > 0)) {
			if (log.isDebugEnabled()) log.debug("Using test.sakai.home: " + sakaiHome);
		} else {
			// For the sake of Eclipse, provide a non-Maven-ish approach.
			sakaiHome = System.getenv("TEST_SAKAI_HOME");
			if ((sakaiHome != null) && (sakaiHome.length() > 0)) {
				if (log.isDebugEnabled()) log.debug("Using TEST_SAKAI_HOME: " + sakaiHome);
			}
		}
		return sakaiHome;
	}

	/**
	 * Builds an array of file URLs from a directory path.
	 * 
	 * @param dirPath
	 * @return
	 * @throws Exception
	 */
	private static URL[] getJarUrls(String dirPath) {
		File dir = new File(dirPath);
		if (log.isInfoEnabled()) log.info("dirPath=" + dirPath + ", dir=" + dir);
		File[] jars = dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if(pathname.getName().startsWith("xml-apis")) {
					return false;
				}
				return true;
			}
		});
		URL[] urls = new URL[jars.length];
		for(int i = 0; i < jars.length; i++) {
			try {
				urls[i] = jars[i].toURL();
			} catch (MalformedURLException e) {
				log.error(e, e);
			}
		}
		return urls;
	}

	private static URL[] getJarUrls(String[] dirPaths) {
		List<URL> jarList = new ArrayList<URL>();
		
		// Add all of the tomcat jars
		for(int i=0; i<dirPaths.length; i++) {
			jarList.addAll(Arrays.asList(getJarUrls(dirPaths[i])));
		}

		URL[] urlArray = new URL[jarList.size()];
		jarList.toArray(urlArray);
		return urlArray;
	}
}
