/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl;

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
 * A code for code copy of BaseRenderEngine, with the call to init() in
 * render(String, RenderContext) removed. Contains code from the Radeox project.
 * 
 * @author andrew
 */
// FIXME: Component
@Slf4j
public class RWikiBaseRenderEngine implements RenderEngine
{

	protected InitialRenderContext initialContext;

	protected FilterPipe fp;

	public RWikiBaseRenderEngine()
	{
		this(new BaseInitialRenderContext());
	}

	public RWikiBaseRenderEngine(InitialRenderContext context)
	{
		this.initialContext = context;
	}

	public void init()
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
