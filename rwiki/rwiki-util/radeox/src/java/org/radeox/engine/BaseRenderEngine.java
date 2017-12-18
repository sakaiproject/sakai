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

package org.radeox.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;
import org.radeox.EngineManager;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.api.macro.Macro;
import org.radeox.engine.context.BaseInitialRenderContext;
import org.radeox.filter.Filter;
import org.radeox.filter.FilterPipe;
import org.radeox.filter.context.BaseFilterContext;
import org.radeox.filter.context.FilterContext;
import org.radeox.macro.MacroRepository;
import org.radeox.util.Service;

/**
 * Base implementation of RenderEngine
 * 
 * @author Stephan J. Schmidt
 * @version $Id: BaseRenderEngine.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */
@Slf4j
public class BaseRenderEngine implements RenderEngine
{
	protected InitialRenderContext initialContext;

	protected FilterPipe fp;

	public BaseRenderEngine(InitialRenderContext context)
	{
		this.initialContext = context;
	}

	public BaseRenderEngine()
	{
		this(new BaseInitialRenderContext());
	}

	protected void init()
	{
		if (null == fp)
		{
			fp = new FilterPipe(initialContext);

			Iterator iterator = Service.providers(Filter.class);
			while (iterator.hasNext())
			{
				try
				{
					Filter filter = (Filter) iterator.next();
					fp.addFilter(filter);
					log.debug("Loaded filter: " + filter.getClass().getName());
				}
				catch (Exception e)
				{
					log.warn("BaseRenderEngine: unable to load filter", e);
				}
			}

			fp.init();
			// Logger.debug("FilterPipe = "+fp.toString());
		}
	}

	/**
	 * Name of the RenderEngine. This is used to get a RenderEngine instance
	 * with EngineManager.getInstance(name);
	 * 
	 * @return name Name of the engine
	 */
	public String getName()
	{
		return EngineManager.DEFAULT;
	}

	/**
	 * Render an input with text markup and return a String with e.g. HTML
	 * 
	 * @param content
	 *        String with the input to render
	 * @param context
	 *        Special context for the filter engine, e.g. with configuration
	 *        information
	 * @return result Output with rendered content
	 */
	public String render(String content, RenderContext context)
	{
		init();
		FilterContext filterContext = new BaseFilterContext();
		filterContext.setRenderContext(context);
		return fp.filter(content, filterContext);
	}

	/**
	 * Render an input with text markup from a Reader and write the result to a
	 * writer
	 * 
	 * @param in
	 *        Reader to read the input from
	 * @param context
	 *        Special context for the render engine, e.g. with configuration
	 *        information
	 */
	public String render(Reader in, RenderContext context) throws IOException
	{
		StringBuffer buffer = new StringBuffer();
		BufferedReader inputReader = new BufferedReader(in);
		String line;
		while ((line = inputReader.readLine()) != null)
		{
			buffer.append(line);
		}
		return render(buffer.toString(), context);
	}

	public void render(Writer out, String content, RenderContext context)
			throws IOException
	{
		out.write(render(content, context));
	}
	
	public void addMacro(Macro macro) {
		MacroRepository mr = MacroRepository.getInstance();
		mr.put(macro.getName(), macro);
	}

}
