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
package org.sakaiproject.search.component.adapter.rwiki;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.model.SearchBuilderItem;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiEntity;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.DigestHtml;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

public class RWikiEntityContentProducer implements EntityContentProducer
{

	private static Log log = LogFactory
			.getLog(RWikiEntityContentProducer.class);

	private RenderService renderService = null;

	private RWikiObjectService objectService = null;

	private SearchService searchService = null;

	private SearchIndexBuilder searchIndexBuilder = null;

	private EntityManager entityManager = null;

	public void init()
	{
		try
		{
			ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
					.getInstance();
			renderService = (RenderService) load(cm, RenderService.class
					.getName());
			objectService = (RWikiObjectService) load(cm,
					RWikiObjectService.class.getName());
			searchService = (SearchService) load(cm, SearchService.class
					.getName());
			searchIndexBuilder = (SearchIndexBuilder) load(cm,
					SearchIndexBuilder.class.getName());
			entityManager = (EntityManager) load(cm, EntityManager.class
					.getName());

			if ("true".equals(ServerConfigurationService.getString(
					"search.experimental", "true")))
			{

				searchService
						.registerFunction(RWikiObjectService.EVENT_RESOURCE_ADD);
				searchService
						.registerFunction(RWikiObjectService.EVENT_RESOURCE_WRITE);
				searchIndexBuilder.registerEntityContentProducer(this);
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to init ", t);
		}

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

	public boolean isContentFromReader(Entity cr)
	{
		return false;
	}

	public Reader getContentReader(Entity cr)
	{
		return null;
	}

	public String getContent(Entity cr)
	{
		RWikiEntity rwe = (RWikiEntity) cr;
		RWikiObject rwo = rwe.getRWikiObject();
		String pageName = rwo.getName();
		String pageSpace = NameHelper.localizeSpace(pageName, rwo.getRealm());
		String renderedPage = renderService.renderPage(rwo, pageSpace,
				objectService.getComponentPageLinkRender(pageSpace));

		return DigestHtml.digest(renderedPage);

	}

	public String getTitle(Entity cr)
	{
		RWikiEntity rwe = (RWikiEntity) cr;
		RWikiObject rwo = rwe.getRWikiObject();
		return rwo.getName();
	}

	public boolean matches(Reference ref)
	{
		EntityProducer ep = ref.getEntityProducer();
		return (ep instanceof RWikiObjectService);
	}

	public List getAllContent()
	{
		List allPages = objectService.findAllPageNames();
		List l = new ArrayList();
		for (Iterator i = allPages.iterator(); i.hasNext();)
		{
			String pageName = (String) i.next();
			String reference = objectService.createReference(pageName);
			l.add(reference);
		}
		return l;
	}

	public Integer getAction(Event event)
	{
		String eventName = event.getEvent();
		if (RWikiObjectService.EVENT_RESOURCE_ADD.equals(eventName)
				|| RWikiObjectService.EVENT_RESOURCE_WRITE.equals(eventName))
		{
			return SearchBuilderItem.ACTION_ADD;
		}
		if (RWikiObjectService.EVENT_RESOURCE_REMOVE.equals(eventName))
		{
			return SearchBuilderItem.ACTION_DELETE;
		}
		return SearchBuilderItem.ACTION_UNKNOWN;
	}

	public boolean matches(Event event)
	{
		return !SearchBuilderItem.ACTION_UNKNOWN.equals(getAction(event));
	}

	public String getTool()
	{
		return "Wiki";
	}

	public String getUrl(Entity entity)
	{
		return entity.getUrl() + "html";
	}

	public String getSiteId(Reference ref)
	{
		String context = ref.getContext();
		if (context.startsWith("/site/"))
		{
			context = context.substring("/site/".length());
		}
		if (context.startsWith("/"))
		{
			context = context.substring(1);
		}
		int slash = context.indexOf("/");
		if (slash > 0)
		{
			context = context.substring(0, slash);
		}
		return context;
	}

	public String getSiteId(String resourceName)
	{

		return getSiteId(entityManager.newReference(resourceName));
	}

	public List getSiteContent(String context)
	{
		List allPages = objectService.findRWikiSubPages("/site/" + context);
		List l = new ArrayList();
		for (Iterator i = allPages.iterator(); i.hasNext();)
		{
			RWikiObject page = (RWikiObject) i.next();
			String reference = objectService.createReference(page.getName());
			l.add(reference);
		}
		return l;
	}

	public boolean isForIndex(Reference ref)
	{

		try
		{
			RWikiEntity rwe = (RWikiEntity) ref.getEntity();
			RWikiObject rwo = rwe.getRWikiObject();
			String pageName = rwo.getName();
			String pageSpace = NameHelper.localizeSpace(pageName, rwo
					.getRealm());
			if (objectService.exists(pageName, pageSpace))
			{
				return true;
			}
		}
		catch (Exception ex)
		{
		}
		return false;
	}

}
