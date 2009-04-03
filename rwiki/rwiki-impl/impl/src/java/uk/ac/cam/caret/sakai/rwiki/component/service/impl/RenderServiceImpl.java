/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.sakaiproject.component.api.ComponentManager;

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

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		renderEngineFactory = (RenderEngineFactory) load(cm, RenderEngineFactory.class
				.getName());

		renderContextFactory = (RenderContextFactory) load(cm, RenderContextFactory.class.getName());
		renderCache = (RenderCache) load(cm, RenderCache.class
				.getName());
	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name);
		}
		return o;
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
			if ( renderedPage.indexOf("<p ") < 0 && renderedPage.indexOf("</p>") < 0   ) 
			{
				renderedPage = "<p class=\"paragraph\">"+renderedPage+"</p>";
			}
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
