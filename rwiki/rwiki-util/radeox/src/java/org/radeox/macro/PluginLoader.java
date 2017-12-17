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

package org.radeox.macro;

import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;
import org.radeox.util.Service;

/**
 * Plugin loader
 * 
 * @author Stephan J. Schmidt
 * @version $Id$
 */
@Slf4j
public abstract class PluginLoader
{
	protected Repository repository;

	public Repository loadPlugins(Repository repository)
	{
		return loadPlugins(repository, getLoadClass());
	}

	public void setRepository(Repository repository)
	{
		this.repository = repository;
	}

	public Iterator getPlugins(Class klass)
	{
		return Service.providers(klass);
	}

	public Repository loadPlugins(Repository repository, Class klass)
	{
		if (null != repository)
		{
			/* load all macros found in the services plugin control file */
			Iterator iterator = getPlugins(klass);
			while (iterator.hasNext())
			{
				try
				{
					Object plugin = iterator.next();
					add(repository, plugin);
					log.debug("PluginLoader: Loaded plugin: "
							+ plugin.getClass());
				}
				catch (Exception e)
				{
					log.warn("PluginLoader: unable to load plugin", e);
				}
			}
		}
		return repository;
	}

	/**
	 * Add a plugin to the known plugin map
	 * 
	 * @param plugin
	 *        Plugin to add
	 */
	public abstract void add(Repository repository, Object plugin);

	public abstract Class getLoadClass();
}
