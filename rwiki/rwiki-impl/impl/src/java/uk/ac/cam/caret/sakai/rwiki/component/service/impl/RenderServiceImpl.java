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
package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;

import uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer;
import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.CachableRenderContext;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.RenderCache;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.RenderContextFactory;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.RenderEngineFactory;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

/**
 * @author andrew
 */
// FIXME: Component WITH FIXES, remove deps on page link render impl
public class RenderServiceImpl implements RenderService
{
	private static Log log = LogFactory.getLog(RenderServiceImpl.class);

	private RenderEngineFactory renderEngineFactory;

	private RenderContextFactory renderContextFactory;

	private RenderCache renderCache;

	public RenderCache getRenderCache()
	{
		return renderCache;
	}

	public void setRenderCache(RenderCache renderCache)
	{
		this.renderCache = renderCache;
	}

	public String renderPage(RWikiObject rwo, String pageSpace,
			PageLinkRenderer plr)
	{

		long start = System.currentTimeMillis();
		String renderedPage = null;
		String cacheKey = getCacheKey(rwo, plr);
		try
		{
			if (plr.canUseCache() && renderCache != null)
			{
				renderedPage = renderCache.getRenderedContent(cacheKey);
				if (renderedPage != null)
				{
					if (TimeLogger.getLogResponse())
						log.info("Cache HIT " + cacheKey);
					else
						log.debug("Cache HIT " + cacheKey);
					return renderedPage;
				}
			}
			else
			{
				log.debug("Render Cache Disabled");
			}
			RenderEngine renderEngine = renderEngineFactory.getRenderEngine(
					pageSpace, plr);
			RenderContext renderContext = renderContextFactory
					.getRenderContext(rwo, renderEngine);
			renderedPage = renderEngine.render(rwo.getContent(), renderContext);
			boolean canCache = false;
			if (renderContext instanceof CachableRenderContext)
			{
				CachableRenderContext crc = (CachableRenderContext) renderContext;
				canCache = crc.isCachable();
			}
			if (canCache && plr.isCachable() && plr.canUseCache())
			{
				if (renderCache != null)
				{
					renderCache.putRenderedContent(cacheKey, renderedPage);
					if (TimeLogger.getLogResponse())
						log.info("Cache PUT " + cacheKey);
					else
						log.debug("Cache PUT " + cacheKey);
				}
				else
				{
					log.debug("Could have cached output");
				}
			}
			else
			{
				if (TimeLogger.getLogResponse())
					log.info("Cant Cache " + cacheKey);
				else
					log.debug("Cant Cache " + cacheKey);
			}
			return renderedPage;
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("Render: " + rwo.getName(), start, finish);
		}
	}

	public RenderContextFactory getRenderContextFactory()
	{
		return renderContextFactory;
	}

	public void setRenderContextFactory(
			RenderContextFactory renderContextFactory)
	{
		this.renderContextFactory = renderContextFactory;
	}

	public RenderEngineFactory getRenderEngineFactory()
	{
		return renderEngineFactory;
	}

	public void setRenderEngineFactory(RenderEngineFactory renderEngineFactory)
	{
		this.renderEngineFactory = renderEngineFactory;
	}

	/**
	 * Generates a key for the page taking into account the page, version and
	 * link render mecahnism
	 * 
	 * @param rwo
	 * @param plr
	 * @return
	 */
	public String getCacheKey(RWikiObject rwo, PageLinkRenderer plr)
	{
		String classNameHash = plr.getClass().getName();
		classNameHash = classNameHash.substring(classNameHash.lastIndexOf("."));
		return rwo.getId() + "." + rwo.getVersion().getTime() + "."
				+ classNameHash;
	}

}
