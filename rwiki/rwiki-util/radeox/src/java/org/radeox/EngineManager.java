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

package org.radeox;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.radeox.api.engine.RenderEngine;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.util.Service;

/**
 * Acess point to dock several different rendering engines into e.g. SnipSnap.
 * Will be replaced by PicoContainer (but kept for compatibility)
 * 
 * @author Stephan J. Schmidt
 * @version $Id: EngineManager.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */
@Slf4j
public class EngineManager
{
	public static final String DEFAULT = "radeox";

	private static Map availableEngines = new HashMap();

	static
	{
		Iterator iterator = Service.providers(RenderEngine.class);
		while (iterator.hasNext())
		{
			try
			{
				RenderEngine engine = (RenderEngine) iterator.next();
				registerEngine(engine);
				log
						.debug("Loaded RenderEngine: "
								+ engine.getClass().getName());
			}
			catch (Exception e)
			{
				log.warn("EngineManager: unable to load RenderEngine", e);
			}
		}
	}

	/**
	 * Different RenderEngines can register themselves with the EngineManager
	 * factory to be available with EngineManager.getInstance();
	 * 
	 * @param engine
	 *        RenderEngine instance, e.g. SnipRenderEngine
	 */
	public static synchronized void registerEngine(RenderEngine engine)
	{
		if (null == availableEngines)
		{
			availableEngines = new HashMap();
		}
		availableEngines.put(engine.getName(), engine);
	}

	/**
	 * Get an instance of a RenderEngine. This is a factory method.
	 * 
	 * @param name
	 *        Name of the RenderEngine to get
	 * @return engine RenderEngine for the requested name
	 */
	public static synchronized RenderEngine getInstance(String name)
	{
		if (null == availableEngines)
		{
			availableEngines = new HashMap();
		}

		// Logger.debug("Engines: " + availableEngines);
		return (RenderEngine) availableEngines.get(name);
	}

	/**
	 * Get an instance of a RenderEngine. This is a factory method. Defaults to
	 * a default RenderEngine. Currently this is a basic EngineManager with no
	 * additional features that is distributed with Radeox.
	 * 
	 * @return engine default RenderEngine
	 */
	public static synchronized RenderEngine getInstance()
	{
		// availableEngines = null;
		if (null == availableEngines)
		{
			availableEngines = new HashMap();
		}

		if (!availableEngines.containsKey(DEFAULT))
		{
			RenderEngine engine = new BaseRenderEngine();
			availableEngines.put(engine.getName(), engine);
		}

		return (RenderEngine) availableEngines.get(DEFAULT);
	}

	public static String getVersion()
	{
		return "0.5.1";
	}
}
