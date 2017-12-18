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

import lombok.extern.slf4j.Slf4j;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.site.api.SiteService;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.RenderContextFactory;

/**
 * @author andrew
 */
// FIXME: Component
@Slf4j
public class RenderContextFactoryImpl implements RenderContextFactory
{

	private RWikiObjectService objectService;

	private RWikiSecurityService securityService;

	private SiteService siteService;

	private EntityManager entityManager;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		objectService = (RWikiObjectService) load(cm, RWikiObjectService.class
				.getName());
		securityService = (RWikiSecurityService) load(cm,
				RWikiSecurityService.class.getName());
		siteService = (SiteService) load(cm, SiteService.class.getName());
		entityManager = (EntityManager) load(cm, EntityManager.class.getName());
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
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.radeox.RenderContextFactory#getRenderContext(uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject,
	 *      org.radeox.api.engine.RenderEngine)
	 */
	public RenderContext getRenderContext(RWikiObject rwo,
			RenderEngine renderEngine)
	{

		SpecializedRenderContext context = new SpecializedRenderContext(rwo,
				objectService, securityService, siteService, entityManager);
		context.setRenderEngine(renderEngine);
		return context;
	}

	

	

	

}
