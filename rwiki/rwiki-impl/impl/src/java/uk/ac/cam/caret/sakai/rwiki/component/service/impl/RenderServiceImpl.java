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
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.ResourceLoader;

import org.apache.commons.lang3.StringUtils;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer;
import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.CachableRenderContext;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.RenderContextFactory;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.RenderEngineFactory;
import uk.ac.cam.caret.sakai.rwiki.service.exception.ReadPermissionException;
import uk.ac.cam.caret.sakai.rwiki.service.exception.UpdatePermissionException;
import uk.ac.cam.caret.sakai.rwiki.service.exception.CreatePermissionException;
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

	private RWikiObjectService objectService;

	private SiteService siteService;

	private RWikiSecurityService wikiSecurityService;

	private static ResourceLoader rl = new ResourceLoader("uk.ac.cam.caret.sakai.rwiki.component.bundle.Messages");

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		renderEngineFactory = (RenderEngineFactory) load(cm, RenderEngineFactory.class
				.getName());

		renderContextFactory = (RenderContextFactory) load(cm, RenderContextFactory.class.getName());

		objectService = (RWikiObjectService) load(cm, RWikiObjectService.class.getName());

		siteService = (SiteService) load(cm, SiteService.class.getName());

		wikiSecurityService = (RWikiSecurityService) load(cm, RWikiSecurityService.class.getName());
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
			
			String content = rwo.getContent();
			int startRwikiName = content.indexOf("[");
			int finishRwikiName = content.indexOf("]");
			while(startRwikiName != -1) {
				String pageGroups = objectService.getRWikiObjectPageGroups(content.substring(startRwikiName+1, finishRwikiName), rwo.getRealm());
				String stPageGroups = "";
				boolean showPage;
				try {
					showPage = objectService.checkRead(objectService.getRWikiObject(content.substring(startRwikiName+1, finishRwikiName), rwo.getRealm()));
				} catch (ReadPermissionException ex) {
					showPage = false;
				} catch (CreatePermissionException ex) {
					showPage = true;
				}
				
				boolean showGroups;
				try {
					showGroups = objectService.checkAdminPermission(objectService.getRWikiObject(content.substring(startRwikiName+1, finishRwikiName), rwo.getRealm()));
				} catch (ReadPermissionException ex) {
					showGroups = false;
				} catch (UpdatePermissionException ex) {
					showGroups = false;
				} catch (CreatePermissionException ex) {
					showGroups = false;
				}
				
				if (StringUtils.isNotBlank(pageGroups) && showGroups) { 
					stPageGroups += " __%%{color:gray}(*" + rl.getString("availableTo.Groups") + pageGroups + "){color}%%__";
				}
				if (!showPage) {
					content = content.substring(0,startRwikiName) + content.substring(finishRwikiName+1);
				} else {
					content = content.substring(0,finishRwikiName+1) + stPageGroups + content.substring(finishRwikiName+1);
				}
				startRwikiName = content.indexOf("[", startRwikiName + 2);
				finishRwikiName = content.indexOf("]", startRwikiName);
			}
			renderedPage = renderEngine.render(content, renderContext);
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
