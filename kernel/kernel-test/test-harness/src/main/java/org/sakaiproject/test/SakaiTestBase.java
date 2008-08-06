/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2005, 2006, 2007, 2008 Sakai Foundation
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PropertyResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import junit.framework.TestCase;

/**
 * An extension of JUnit's TestCase that launches the Sakai component manager.
 * Extend this class to run tests in a simulated Sakai environment.
 * 
 * <p>
 * <strong>NOTE:</strong>
 * Starting the component manager is an expensive operation, since it loads all
 * of the service implementations in the system, including database connection
 * pools, hibernate mappings, etc.  To run a test suite, please collect all tests
 * into a single class rather than running a variety of individual test cases.
 * See {@link org.sakaiproject.test.SakaiIntegrationTest} for an example.
 * </p>
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public abstract class SakaiTestBase extends TestCase {
	private static final Log log = LogFactory.getLog(SakaiTestBase.class);
	protected static Object compMgr;
	
	/**
	 * Initialize the component manager once for all tests, and log in as admin.
	 */
	protected static void oneTimeSetup() throws Exception {
		if(compMgr == null) {
			// Find the sakai home dir
			String tomcatHome = getTomcatHome();
			String sakaiHome = getSakaiHome(tomcatHome);
			String componentsDir = tomcatHome + "components/";
			
			// Set the system properties needed by the sakai component manager
			System.setProperty("sakai.home", sakaiHome);
			System.setProperty("sakai.components.root", componentsDir);

			log.debug("Starting the component manager");

			// Add the sakai jars to the current classpath.  Note:  We are limited to using the sun jvm now
			URL[] sakaiUrls = getJarUrls(new String[] {tomcatHome + "common/endorsed/",
					tomcatHome + "common/lib/", tomcatHome + "shared/lib/"});
			URLClassLoader appClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
			Method addMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] {URL.class});
			addMethod.setAccessible(true);
			for(int i=0; i<sakaiUrls.length; i++) {
				addMethod.invoke(appClassLoader, new Object[] {sakaiUrls[i]});
			}
			
			Class clazz = Class.forName("org.sakaiproject.component.cover.ComponentManager");
			compMgr = clazz.getDeclaredMethod("getInstance", (Class[])null).invoke((Object[])null, (Object[])null);

			log.debug("Finished starting the component manager");
		}
	}

	/**
	 * Close the component manager when the tests finish.
	 */
	public static void oneTimeTearDown() {
		//SessionManager.getCurrentSession().invalidate();
		if(compMgr != null) {
			try {
				Method closeMethod = compMgr.getClass().getMethod("close", new Class[0]);
				closeMethod.invoke(compMgr, new Object[0]);
			} catch (Exception e) {
				log.error(e);
			}
		}
	}

	/**
	 * Fetches the "maven.tomcat.home" property from the maven build.properties
	 * file located in the user's $HOME directory.
	 * 
	 * @return
	 * @throws Exception
	 */
	private static String getTomcatHome() throws Exception {
		String testTomcatHome = System.getProperty("test.tomcat.home");
		if ( testTomcatHome != null && testTomcatHome.length() > 0 ) {
			log.debug("Using tomcat home: " + testTomcatHome);
			return testTomcatHome;
		} else {
			String homeDir = System.getProperty("user.home");
			File file = new File(homeDir + File.separatorChar + "build.properties");
			FileInputStream fis = new FileInputStream(file);
			PropertyResourceBundle rb = new PropertyResourceBundle(fis);
			String tomcatHome = rb.getString("maven.tomcat.home");
			log.debug("Tomcat home = " + tomcatHome);
			return tomcatHome;
		}
	}
	
	private static String getSakaiHome(String tomcatHome) throws Exception {
		String sakaiHome = System.getProperty("test.sakai.home");
		if (sakaiHome == null) {
			sakaiHome = tomcatHome + File.separatorChar + "sakai" + File.separatorChar;
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
	private static URL[] getJarUrls(String dirPath) throws Exception {
		File dir = new File(dirPath);
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
			urls[i] = jars[i].toURL();
		}
		return urls;
	}

	private static URL[] getJarUrls(String[] dirPaths) throws Exception {
		List jarList = new ArrayList();
		
		// Add all of the tomcat jars
		for(int i=0; i<dirPaths.length; i++) {
			jarList.addAll(Arrays.asList(getJarUrls(dirPaths[i])));
		}

		URL[] urlArray = new URL[jarList.size()];
		jarList.toArray(urlArray);
		return urlArray;
	}
	
	/**
	 * Convenience method to get a service bean from the Sakai component manager.
	 * 
	 * @param beanId The id of the service
	 * 
	 * @return The service, or null if the ID is not registered
	 */
	protected static final Object getService(String beanId) {
		try {
			Method getMethod = compMgr.getClass().getMethod("get", new Class[] {String.class});
			return getMethod.invoke(compMgr, new Object[] {beanId});
		} catch (Exception e) {
			log.error(e);
			return null;
		}
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
}
