/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radeox.EngineManager;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.context.BaseInitialRenderContext;
import org.radeox.filter.Filter;
import org.radeox.filter.FilterPipe;
import org.radeox.filter.context.BaseFilterContext;
import org.radeox.filter.context.FilterContext;
import org.radeox.util.Service;

/**
 * A code for code copy of BaseRenderEngine, with the call to init() in
 * render(String, RenderContext) removed. Contains code from the Radeox project.
 * 
 * @author andrew
 */
// FIXME: Component
public class RWikiBaseRenderEngine implements RenderEngine
{
	private static Log log = LogFactory.getLog(RWikiBaseRenderEngine.class);

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

}
