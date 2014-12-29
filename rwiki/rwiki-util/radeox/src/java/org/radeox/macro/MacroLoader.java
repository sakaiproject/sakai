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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Plugin loader for macros
 * 
 * @author Stephan J. Schmidt
 * @version $Id$
 */

public class MacroLoader extends PluginLoader
{
	private static Log log = LogFactory.getLog(MacroLoader.class);

	public Class getLoadClass()
	{
		return Macro.class;
	}

	/**
	 * Add a plugin to the known plugin map
	 * 
	 * @param macro
	 *        Macro to add
	 */
	public void add(Repository repository, Object plugin)
	{
		if (plugin instanceof org.radeox.api.macro.Macro)
		{
			repository.put(((org.radeox.api.macro.Macro) plugin).getName(), plugin);
		}
		else
		{
			log.warn("MacroLoader: " + plugin.getClass() + " not of Type "
					+ org.radeox.api.macro.Macro.class);
		}
	}

}
