/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * <p>
 * Load the available Sakai components into the shared component manager's Spring ApplicationContext
 * </p>
 */
@Slf4j
public class ComponentsLoader
{
	/** Folder containing override definitions for beans */
	private File overridesFolder;
	
	public ComponentsLoader()
	{
		this(null);
	}

	public ComponentsLoader(File overridesFolder)
	{
		this.overridesFolder = overridesFolder;
	}

	/**
	 * 
	 */
	public void load(ConfigurableApplicationContext ac, String componentsRoot)
	{
		try
		{
			// get a list of the folders in the root
			File root = new File(componentsRoot);

			// make sure it's a dir.
			if (!root.isDirectory())
			{
				log.warn("load: root not directory: " + componentsRoot);
				return;
			}

			// what component packages are there?
			File[] packageArray = root.listFiles();

			if (packageArray == null)
			{
				log.warn("load: empty directory: " + componentsRoot);
				return;
			}
 			List<File> packages = Arrays.asList(packageArray);

 			// assure a consistent order - sort these files
 			Collections.sort(packages);

			// for testing, we might reverse load order
 			if (System.getProperty("sakai.components.reverse.load") != null) {
 				Collections.reverse(packages);
 			}
			log.info("load: loading components from: " + componentsRoot);

			// process the packages
			for (File packageDir : packages)
			{
				// if a valid components directory
				if (validComponentsPackage(packageDir))
				{
					loadComponentPackage(packageDir, ac);
				}
				else
				{
					log.warn("load: skipping non-package entry: " + packageDir);
				}
			}
		}
		catch (Exception e) {
			log.error("load: exception: " + e, e);
		}
	}

	/**
	 * Load one component package into the AC
	 * 
	 * @param dir
	 *        The file path to the component package
	 * @param ac
	 *        The ApplicationContext to load into
	 */
	protected void loadComponentPackage(File dir, ConfigurableApplicationContext ac)
	{
		// setup the classloader onto the thread
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		ClassLoader loader = newPackageClassLoader(dir);

		log.info("loadComponentPackage: " + dir);

		Thread.currentThread().setContextClassLoader(loader);

		File xml = null;

		try
		{
			// load this xml file
			File webinf = new File(dir, "WEB-INF");
			xml = new File(webinf, "components.xml");

			// make a reader
			XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader((BeanDefinitionRegistry) ac.getBeanFactory());
			
			// In Spring 2, classes aren't loaded during bean parsing unless this
			// classloader property is set.
			reader.setBeanClassLoader(loader);
			
			List<Resource> beanDefList = new ArrayList<Resource>();
			beanDefList.add(new FileSystemResource(xml.getCanonicalPath()));
			
			// Load the demo components, if necessary
			File demoXml = new File(webinf, "components-demo.xml");
			if("true".equalsIgnoreCase(System.getProperty("sakai.demo")))
			{
				if(log.isDebugEnabled()) log.debug("Attempting to load demo components");
				if(demoXml.exists())
				{
					if(log.isInfoEnabled()) log.info("Loading demo components from " + dir);
					beanDefList.add(new FileSystemResource(demoXml.getCanonicalPath()));
				}
			}
			else
			{
				if(demoXml.exists())
				{
					// Only log that we're skipping the demo components if they exist
					if(log.isInfoEnabled()) log.info("Skipping demo components from " + dir);
				}
			}
			if (overridesFolder != null) {
				File override = new File(overridesFolder, dir.getName()+ ".xml");
				if (override.isFile()) {
					beanDefList.add(new FileSystemResource(override.getCanonicalPath()));
					if(log.isInfoEnabled()) log.info("Overriding component definitions with "+ override);
				}
			}
			reader.loadBeanDefinitions(beanDefList.toArray(new Resource[0]));
		}
		catch (Exception e)
		{
			log.error("loadComponentPackage: exception loading: " + xml + " : " + e, e);
		}
		finally
		{
			// restore the context loader
			Thread.currentThread().setContextClassLoader(current);
		}
	}

	/**
	 * Test if this File is a valid components package directory.
	 * 
	 * @param dir
	 *        The file to test
	 * @return true if it is a valid components package directory, false if not.
	 */
	protected boolean validComponentsPackage(File dir)
	{
		// valid if this is a directory with a WEB-INF directory below with a components.xml file
		if ((dir != null) && (dir.isDirectory()))
		{
			File webinf = new File(dir, "WEB-INF");
			if ((webinf != null) && (webinf.isDirectory()))
			{
				File xml = new File(webinf, "components.xml");
				if ((xml != null) && (xml.isFile()))
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Create the class loader for this component package
	 * 
	 * @param dir
	 *        The package's root directory.
	 * @return A class loader, whose parent is this class's loader, which has the classes/ and jars for this component.
	 */
	protected ClassLoader newPackageClassLoader(File dir)
	{
		// collect as a List, turn into an array after
		List urls = new Vector();

		File webinf = new File(dir, "WEB-INF");

		// put classes/ on the classpath
		File classes = new File(webinf, "classes");
		if ((classes != null) && (classes.isDirectory()))
		{
			try {
                URL url = new URL("file:" + classes.getCanonicalPath() + "/");
                urls.add(url);
            } catch (Exception e) {
                log.warn("Bad url for classes: "+classes.getPath()+" : "+e);
            }
		}

		// put each .jar file onto the classpath
		File lib = new File(webinf, "lib");
		if ((lib != null) && (lib.isDirectory()))
		{
			File[] jars = lib.listFiles(new FileFilter()
			{
				public boolean accept(File file)
				{
					return (file.isFile() && file.getName().endsWith(".jar"));
				}
			});

			if (jars != null)
			{
				// We sort them so that we get predictable results when loading classes.
				// Otherwise sometimes you can end up with one class and other times you can end up with another
				// depending on the order the listing is stored on the filesystem.
				Arrays.sort(jars);
				for (int j = 0; j < jars.length; j++)
				{
				    if (jars[j] != null) {
	                    try {
	                        URL url = new URL("file:" + jars[j].getCanonicalPath());
	                        urls.add(url);
	                    } catch (Exception e) {
	                        log.warn("Bad url for jar: "+jars[j].getPath()+" : "+e);
	                    }
				    }
				}
			}
		}

		// make the array from the list
		URL[] urlArray = (URL[]) urls.toArray(new URL[urls.size()]);
		ClassLoader loader = null;

		// Check to see if Terracotta clustering is turned on
		// String clusterTerracotta = ServerConfigurationService.getString("cluster.terracotta","false");
		String clusterTerracotta = System.getProperty("sakai.cluster.terracotta");
		
		if ("true".equals(clusterTerracotta)) {
			// If Terracotta clustering is turned on then use the Special Terracotta Class loader
			loader = new TerracottaClassLoader(urlArray, getClass().getClassLoader(), dir.getName());
		} else {
			// Terracotta clustering is turned off, so use the normal URLClassLoader
			loader = new URLClassLoader(urlArray, getClass().getClassLoader());
		}

		return loader;
	}

}
