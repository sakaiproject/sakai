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

package org.radeox.macro.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.radeox.macro.PluginRepository;
import org.radeox.macro.Repository;

/**
 * Repository for functions
 * 
 * @author Stephan J. Schmidt
 * @version $Id: FunctionRepository.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */

public class FunctionRepository extends PluginRepository
{
	protected static Repository instance;

	protected List loaders;

	public synchronized static Repository getInstance()
	{
		if (null == instance)
		{
			instance = new FunctionRepository();
		}
		return instance;
	}

	private void load()
	{
		Iterator iterator = loaders.iterator();
		while (iterator.hasNext())
		{
			FunctionLoader loader = (FunctionLoader) iterator.next();
			loader.loadPlugins(this);
		}
	}

	private FunctionRepository()
	{
		loaders = new ArrayList();
		loaders.add(new FunctionLoader());

		load();
	}
}
