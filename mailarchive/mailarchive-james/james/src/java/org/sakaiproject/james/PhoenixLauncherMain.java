/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.james;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Taken from the phoenix 4.0.4 org.apache.avalon.phoenix.launcher.Main.java */
/*
 * Copyright (C) The Apache Software Foundation. All rights reserved. This software is published under the terms of the Apache Software License version 1.1, a copy of which has been included with this distribution in the LICENSE.txt file.
 */

/**
 * PhoenixLoader is the class that bootstraps and sets up engine ClassLoader. It also sets up a default policy that gives full permissions to engine code.
 * 
 * @author <a href="mailto:peter at apache.org">Peter Donald</a>
 */
public final class PhoenixLauncherMain
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(PhoenixLauncherMain.class);

	private static final String MAIN_CLASS = "org.apache.avalon.phoenix.frontends.CLIMain";

	private static final String LOADER_JAR = "phoenix-loader.jar";

	private static Object c_frontend;

	/**
	 * Main entry point for Phoenix.
	 * 
	 * @param args
	 *        the command line arguments
	 * @throws Exception
	 *         if an error occurs
	 */
	public static final void main(final String[] args) throws Exception
	{
		final int exitCode = startup(args, new HashMap(), true);
		System.exit(exitCode);
	}

	/**
	 * Method to call to startup Phoenix from an external (calling) application. Protected to allow access from DaemonLauncher.
	 * 
	 * @param args
	 *        the command line arg array
	 * @param data
	 *        a set of extra parameters to pass to embeddor
	 * @param blocking
	 *        false if the current thread is expected to return.
	 * @return the exit code which should be used to exit the JVM
	 * @throws Exception
	 *         if an error occurs
	 */
	protected static final int startup(final String[] args, final Map data, final boolean blocking) throws Exception
	{
		int exitCode;
		try
		{
			// setup new Policy manager
			// TODO: (done) removed
			// Policy.setPolicy( new FreeNEasyPolicy() );

			// Create engine ClassLoader
			// TODO: (done) removed
			// final URL[] urls = getEngineClassPath();
			// final URLClassLoader classLoader = new URLClassLoader( urls );

			// TODO: add these extra paths to the existing classloader
			final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

			// TODO: (done) use the one classloader
			// data.put( "common.classloader", ClassLoader.getSystemClassLoader() );
			data.put("common.classloader", classLoader);

			data.put("container.classloader", classLoader);
			data.put("phoenix.home", new File(findPhoenixHome()));

			// Setup context classloader
			// TODO: (done) removed
			// Thread.currentThread().setContextClassLoader( classLoader );

			// Create main launcher
			final Class clazz = classLoader.loadClass(MAIN_CLASS);
			final Class[] paramTypes = new Class[] { args.getClass(), Map.class, Boolean.TYPE };
			final Method method = clazz.getMethod("main", paramTypes);
			c_frontend = clazz.newInstance();

			// kick the tires and light the fires....
			final Integer integer = (Integer) method.invoke(c_frontend, new Object[] { args, data, Boolean.valueOf(blocking) });
			exitCode = integer.intValue();
		}
		catch (final Exception e)
		{
			// TODO: (done) changed to a log
			// e.printStackTrace();
			M_log.warn("PhoenixLauncherMain.startup: " + e);
			exitCode = 1;
		}
		return exitCode;
	}

	/**
	 * Method to call to shutdown Phoenix from an external (calling) application. Protected to allow access from DaemonLauncher.
	 */
	protected static final void shutdown()
	{
		if (null == c_frontend)
		{
			return;
		}

		try
		{
			final Class clazz = c_frontend.getClass();
			final Method method = clazz.getMethod("shutdown", new Class[0]);

			// Lets put this sucker to sleep
			method.invoke(c_frontend, new Object[0]);
		}
		catch (final Exception e)
		{
			// TODO: (done) changed to a log
			// e.printStackTrace();
			M_log.warn("PhoenixLauncherMain.shutdown: " + e);
		}
		finally
		{
			c_frontend = null;
		}
	}

	/**
	 * Create a ClassPath for the engine.
	 * 
	 * @return the set of URLs that engine uses to load
	 * @throws Exception
	 *         if unable to aquire classpath
	 */
	private static URL[] getEngineClassPath() throws Exception
	{
		final ArrayList urls = new ArrayList();

		final File dir = findEngineLibDir();
		final File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			final File file = files[i];
			if (file.getName().endsWith(".jar"))
			{
				urls.add(file.toURL());
			}
		}

		// TODO: (done) add also the phoenix libs
		final File dir_p = findPhoenixLibDir();
		final File[] files_p = dir_p.listFiles();
		for (int i = 0; i < files_p.length; i++)
		{
			final File file = files_p[i];
			if (file.getName().endsWith(".jar"))
			{
				urls.add(file.toURL());
			}
		}

		return (URL[]) urls.toArray(new URL[urls.size()]);
	}

	/**
	 * Find directory to load engine specific libraries from.
	 * 
	 * @return the lib dir
	 * @throws Exception
	 *         if unable to aquire directory
	 */
	private static File findEngineLibDir() throws Exception
	{
		final String phoenixHome = findPhoenixHome();
		final String engineLibDir = phoenixHome + File.separator + "bin" + File.separator + "lib";
		final File dir = new File(engineLibDir).getCanonicalFile();
		if (!dir.exists())
		{
			throw new Exception("Unable to locate engine lib directory at " + engineLibDir);
		}
		return dir;
	}

	/**
	 * TODO: (done) added this method Find directory to load phoenix libraries from.
	 * 
	 * @return the lib dir
	 * @throws Exception
	 *         if unable to aquire directory
	 */
	private static File findPhoenixLibDir() throws Exception
	{
		final String phoenixHome = findPhoenixHome();
		final String engineLibDir = phoenixHome + File.separator + "lib";
		final File dir = new File(engineLibDir).getCanonicalFile();
		if (!dir.exists())
		{
			throw new Exception("Unable to locate engine lib directory at " + engineLibDir);
		}
		return dir;
	}

	/**
	 * Utility method to find the home directory of Phoenix and make sure system property is set to it.
	 * 
	 * @return the location of phoenix directory
	 * @throws Exception
	 *         if unable to locate directory
	 */
	private static String findPhoenixHome() throws Exception
	{
		String phoenixHome = System.getProperty("phoenix.home", null);
		if (null == phoenixHome)
		{
			final File loaderDir = findLoaderDir();
			phoenixHome = loaderDir.getAbsoluteFile().getParentFile() + File.separator;
		}

		phoenixHome = (new File(phoenixHome)).getCanonicalFile().toString();
		System.setProperty("phoenix.home", phoenixHome);
		return phoenixHome;
	}

	/**
	 * Finds the LOADER_JAR file in the classpath.
	 */
	private static final File findLoaderDir() throws Exception
	{
		final String classpath = System.getProperty("java.class.path");
		final String pathSeparator = System.getProperty("path.separator");
		final StringTokenizer tokenizer = new StringTokenizer(classpath, pathSeparator);

		while (tokenizer.hasMoreTokens())
		{
			final String element = tokenizer.nextToken();

			if (element.endsWith(LOADER_JAR))
			{
				File file = (new File(element)).getCanonicalFile();
				file = file.getParentFile();
				return file;
			}
		}

		throw new Exception("Unable to locate " + LOADER_JAR + " in classpath");
	}

	/**
	 * Default polic class to give every code base all permssions. Will be replaced once the kernel loads.
	 */
	private static class FreeNEasyPolicy extends Policy
	{
		public PermissionCollection getPermissions(final CodeSource codeSource)
		{
			final Permissions permissions = new Permissions();
			permissions.add(new java.security.AllPermission());
			return permissions;
		}

		public void refresh()
		{
		}
	}
}
