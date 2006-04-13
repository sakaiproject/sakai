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
import java.util.List;
import java.util.Map;

/**
 * Repository for plugins
 * 
 * @author Stephan J. Schmidt
 * @version $Id: PluginRepository.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */

public class PluginRepository implements Repository
{
	protected Map plugins;

	protected List list;

	protected static Repository instance;

	public PluginRepository()
	{
		plugins = new HashMap();
		list = new ArrayList();
	}

	public boolean containsKey(String key)
	{
		return plugins.containsKey(key);
	}

	public Object get(String key)
	{
		return plugins.get(key);
	}

	public List getPlugins()
	{
		return new ArrayList(plugins.values());
	}

	public void put(String key, Object value)
	{
		plugins.put(key, value);
		list.add(value);
	}
}
