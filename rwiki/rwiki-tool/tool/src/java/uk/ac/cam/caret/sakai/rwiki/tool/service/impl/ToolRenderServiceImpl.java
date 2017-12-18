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

package uk.ac.cam.caret.sakai.rwiki.tool.service.impl;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ComponentManager;

import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.tool.api.ToolRenderService;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

/**
 * @author andrew
 */
@Slf4j
public class ToolRenderServiceImpl implements ToolRenderService
{

	private RenderService renderService = null;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();

		renderService = (RenderService) load(cm, RenderService.class.getName());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RenderService#publicRenderPage(uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiObject,
	 *      java.lang.String)
	 */
	public String renderPublicPage(RWikiObject rwo, boolean withBreadCrumbs)
	{
		return renderPublicPage(rwo, rwo.getRealm(), withBreadCrumbs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RenderService#publicRenderPage(uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiObject,
	 *      java.lang.String, java.lang.String)
	 */
	public String renderPublicPage(RWikiObject rwo, String defaultRealm,
			boolean withBreadCrumbs)
	{
		// SAK-2519
		String localSpace = NameHelper.localizeSpace(rwo.getName(),
				defaultRealm);
		PublicPageLinkRendererImpl plr = new PublicPageLinkRendererImpl(
				localSpace, defaultRealm, withBreadCrumbs);
		return renderService.renderPage(rwo, localSpace, plr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RenderService#renderPage(uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject)
	 */
	public String renderPage(RWikiObject rwo)
	{
		return renderPage(rwo, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RenderService#renderPage(uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject,
	 *      java.lang.String)
	 */
	public String renderPage(RWikiObject rwo, String defaultRealm)
	{
		// SAK-2519
		String localSpace = NameHelper.localizeSpace(rwo.getName(),
				defaultRealm);
		PageLinkRendererImpl plr = new PageLinkRendererImpl(localSpace,
				defaultRealm);
		return renderService.renderPage(rwo, localSpace, plr);
	}

	public String renderPage(RWikiObject rwo, boolean cachable)
	{
		// SAK-2519
		String localSpace = NameHelper.localizeSpace(rwo.getName(), rwo
				.getRealm());
		PageLinkRendererImpl plr = new PageLinkRendererImpl(localSpace, rwo
				.getRealm());
		plr.setUseCache(cachable);
		plr.setCachable(cachable);
		return renderService.renderPage(rwo, localSpace, plr);
	}

	

}
