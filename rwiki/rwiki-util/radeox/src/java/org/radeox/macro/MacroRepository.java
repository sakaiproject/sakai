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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.macro.Macro;

/**
 * Repository for plugins
 * 
 * @author Stephan J. Schmidt
 * @version $Id: MacroRepository.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */
@Slf4j
public class MacroRepository extends PluginRepository
{
	private InitialRenderContext context;

	protected static MacroRepository instance;

	protected List loaders;

	public synchronized static MacroRepository getInstance()
	{
		if (null == instance)
		{
			instance = new MacroRepository();
		}
		return instance;
	}

	private void initialize(InitialRenderContext context)
	{
		Iterator iterator = list.iterator();
		while (iterator.hasNext())
		{
			Macro macro = (Macro) iterator.next();
			macro.setInitialContext(context);
		}
		init();
	}

	public void setInitialContext(InitialRenderContext context)
	{
		this.context = context;
		initialize(context);
	}

	private void init()
	{
		Map newPlugins = new HashMap();

		Iterator iterator = list.iterator();
		while (iterator.hasNext())
		{
			Macro macro = (Macro) iterator.next();
			newPlugins.put(macro.getName(), macro);
		}
		plugins = newPlugins;
	}

	/**
	 * Loads macros from all loaders into plugins.
	 */
	private void load()
	{
		Iterator iterator = loaders.iterator();
		while (iterator.hasNext())
		{
			MacroLoader loader = (MacroLoader) iterator.next();
			loader.setRepository(this);
			log.debug("Loading from: " + loader.getClass());
			loader.loadPlugins(this);
		}
	}

	public void addLoader(MacroLoader loader)
	{
		loader.setRepository(this);
		loaders.add(loader);
		plugins = new HashMap();
		list = new ArrayList();
		load();
	}

	private MacroRepository()
	{
		loaders = new ArrayList();
		loaders.add(new MacroLoader());
		load();
	}
}
