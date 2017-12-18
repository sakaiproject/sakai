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

package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.sakaiproject.component.api.ComponentManager;

import uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer;
import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.CachableRenderContext;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.RenderContextFactory;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.RenderEngineFactory;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

/**
 * @author andrew
 */
// FIXME: Component WITH FIXES, remove deps on page link render impl
@Slf4j
public class RenderServiceImpl implements RenderService
{
	private RenderEngineFactory renderEngineFactory;

	private RenderContextFactory renderContextFactory;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		renderEngineFactory = (RenderEngineFactory) load(cm, RenderEngineFactory.class
				.getName());

		renderContextFactory = (RenderContextFactory) load(cm, RenderContextFactory.class.getName());
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
		try
		{
			RenderEngine renderEngine = renderEngineFactory.getRenderEngine(
					pageSpace, plr);
			RenderContext renderContext = renderContextFactory
					.getRenderContext(rwo, renderEngine);
			renderedPage = renderEngine.render(rwo.getContent(), renderContext);
			if ( renderedPage.indexOf("<p ") < 0 && renderedPage.indexOf("</p>") < 0   ) 
			{
				renderedPage = "<p class=\"paragraph\">"+renderedPage+"</p>";
			}
			return renderedPage;
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("Render: " + rwo.getName(), start, finish);
		}
	}

}
