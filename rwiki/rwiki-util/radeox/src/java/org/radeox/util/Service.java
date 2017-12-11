/*
 * This file is part of "SnipSnap Radeox Rendering Engine".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://radeox.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --LICENSE NOTICE--
 */

package org.radeox.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * After the Service class from Sun and the Apache project. With help from
 * Frdric Miserey.
 * 
 * @credits Frdric Miserey, Joseph Oettinger
 * @author Matthias L. Jugel
 * @version $id$
 */
@Slf4j
public class Service
{

	static HashMap services = new HashMap();

	public static synchronized Iterator providerClasses(Class cls)
	{
		return providers(cls, false);
	}

	public static synchronized Iterator providers(Class cls)
	{
		return providers(cls, true);
	}

	public static synchronized Iterator providers(Class cls, boolean instantiate)
	{
		ClassLoader classLoader = cls.getClassLoader();
		String providerFile = "META-INF/services/" + cls.getName();

		// check whether we already loaded the provider classes
		List providers = (List) services.get(providerFile);
		if (providers != null)
		{
			return providers.iterator();
		}

		// create new list of providers
		providers = new ArrayList();
		services.put(providerFile, providers);

		try
		{
			Enumeration providerFiles = classLoader.getResources(providerFile);

			if (providerFiles.hasMoreElements())
			{
				// cycle through the provider files and load classes
				while (providerFiles.hasMoreElements())
				{
					try
					{
						URL url = (URL) providerFiles.nextElement();
						Reader reader = new InputStreamReader(url.openStream(),
								"UTF-8");
						if (instantiate)
						{
							loadResource(reader, classLoader, providers);
						}
						else
						{
							loadClasses(reader, classLoader, providers);
						}
					}
					catch (Exception ex)
					{
						// Just try the next file...
					}
				}
			}
			else
			{
				// Workaround for broken classloaders, e.g. Orion
				InputStream is = classLoader.getResourceAsStream(providerFile);
				if (is == null)
				{
					providerFile = providerFile.substring(providerFile
							.lastIndexOf('.') + 1);
					is = classLoader.getResourceAsStream(providerFile);
				}
				if (is != null)
				{
					Reader reader = new InputStreamReader(is, "UTF-8");
					loadResource(reader, classLoader, providers);
				}
			}
		}
		catch (IOException ioe)
		{
			// ignore exception
		}
		return providers.iterator();
	}

	private static List loadClasses(Reader input, ClassLoader classLoader,
			List classes) throws IOException
	{
		BufferedReader reader = new BufferedReader(input);

		String line = reader.readLine();
		while (line != null)
		{
			try
			{
				// First strip any comment...
				int idx = line.indexOf('#');
				if (idx != -1)
				{
					line = line.substring(0, idx);
				}

				// Trim whitespace.
				line = line.trim();

				// load class if a line was left
				if (line.length() > 0)
				{
					// Try and load the class
					classes.add(classLoader.loadClass(line));
				}
			}
			catch (Exception ex)
			{
				// Just try the next line
			}
			line = reader.readLine();
		}
		return classes;
	}

	private static void loadResource(Reader input, ClassLoader classLoader,
			List providers) throws IOException
	{
		List classes = new ArrayList();
		loadClasses(input, classLoader, classes);
		Iterator iterator = classes.iterator();
		while (iterator.hasNext())
		{
			Class klass = (Class) iterator.next();
			try
			{
				Object obj = klass.newInstance();
				// stick it into our vector...
				providers.add(obj);
			}
			catch (InstantiationException e)
			{
				log.error(e.getMessage(), e);
			}
			catch (IllegalAccessException e)
			{
				log.error(e.getMessage(), e);
			}
			// Logger.debug("Service: loaded "+ obj.getClass().getName());
		}
	}
}
