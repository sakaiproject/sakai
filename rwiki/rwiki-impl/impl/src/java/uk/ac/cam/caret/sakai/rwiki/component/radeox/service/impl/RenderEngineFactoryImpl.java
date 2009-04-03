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

package uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radeox.api.engine.RenderEngine;
import org.sakaiproject.component.api.ComponentManager;

import uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.RenderEngineFactory;

/**
 * @author andrew
 */
// FIXME: Component
public class RenderEngineFactoryImpl implements RenderEngineFactory
{
	private static Log log = LogFactory.getLog(RenderEngineFactoryImpl.class);

	private RWikiObjectService objectService;

	private RenderEngine deligate;

	private String externalImageLink;
	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		objectService = (RWikiObjectService) load(cm, RWikiObjectService.class
				.getName());
		deligate = (RenderEngine) load(cm, RenderEngine.class
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
	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RenderEngineFactory#getRenderEngine(java.lang.String,
	 *      uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer)
	 */
	public RenderEngine getRenderEngine(String space, PageLinkRenderer plr)
	{
		SpecializedRenderEngine renderEngine = new SpecializedRenderEngine(
				deligate, objectService, plr, space, externalImageLink);
		return renderEngine;
	}


	public String getExternalImageLink()
	{
		return externalImageLink;
	}

	public void setExternalImageLink(String externalImageLink)
	{
		this.externalImageLink = externalImageLink;
	}

}
