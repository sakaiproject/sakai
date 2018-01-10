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

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

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
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.util.HTMLParser;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiEntity;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

@Slf4j
public class RWikiEntityContentProducer implements EntityContentProducer
{

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
			renderService = (RenderService) load(cm, RenderService.class.getName());
			objectService = (RWikiObjectService) load(cm, RWikiObjectService.class
					.getName());
			searchService = (SearchService) load(cm, SearchService.class.getName());
			searchIndexBuilder = (SearchIndexBuilder) load(cm, SearchIndexBuilder.class
					.getName());
			entityManager = (EntityManager) load(cm, EntityManager.class.getName());

			if ( "true".equals(ServerConfigurationService.getString(
					"search.enable", "false")))
			{

				searchService.registerFunction(RWikiObjectService.EVENT_RESOURCE_ADD);
				searchService.registerFunction(RWikiObjectService.EVENT_RESOURCE_WRITE);
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

	public boolean isContentFromReader(String cr)
	{
		return false;
	}

	public Reader getContentReader(String reference)
	{
		return null;
	}

	public String getContent(String reference)
	{
		Reference ref = getReference(reference);
		Entity cr = ref.getEntity();
		RWikiEntity rwe = (RWikiEntity) cr;
		RWikiObject rwo = rwe.getRWikiObject();
		String pageName = rwo.getName();
		String pageSpace = NameHelper.localizeSpace(pageName, rwo.getRealm());
		String renderedPage = renderService.renderPage(rwo, pageSpace, objectService
				.getComponentPageLinkRender(pageSpace,true));
		StringBuilder sb = new StringBuilder();
		for (HTMLParser hp = new HTMLParser(renderedPage); hp.hasNext();)
		{
			SearchUtils.appendCleanString(hp.next(), sb);
		}

		String r = sb.toString();
		if (log.isDebugEnabled())
		{
			log.debug("Wiki.getContent:" + reference + ":" + r);
		}
		return r;
	}

	public String getTitle(String reference)
	{
		Reference ref = getReference(reference);
		Entity cr = ref.getEntity();
		RWikiEntity rwe = (RWikiEntity) cr;
		RWikiObject rwo = rwe.getRWikiObject();
		String r = SearchUtils.appendCleanString(rwo.getName(), null).toString();
		if (log.isDebugEnabled())
		{
			log.debug("Wiki.getTitle:" + reference + ":" + r);
		}
		return r;
	}

	public boolean matches(String reference)
	{
		try
		{
			Reference ref = getReference(reference);
			EntityProducer ep = ref.getEntityProducer();
			return (ep instanceof RWikiObjectService);
		}
		catch (Exception ex)
		{
			return false;
		}

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
		return "wiki";
	}

	public String getUrl(String reference)
	{
		Reference ref = getReference(reference);
		return ref.getUrl() + "html";
	}

	private String getSiteId(Reference ref)
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
		if (log.isDebugEnabled())
		{
			log.debug("Wiki.getSiteId" + ref + ":" + context);
		}
		return context;
	}

	public String getSiteId(String resourceName)
	{

		String r = getSiteId(entityManager.newReference(resourceName));
		if (log.isDebugEnabled())
		{
			log.debug("Wiki.getSiteId" + resourceName + ":" + r);
		}
		return r;
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

	public Iterator getSiteContentIterator(String context)
	{
		List<RWikiObject> allPages = objectService.findRWikiSubPages("/site/" + context);
		final Iterator<RWikiObject> allPagesIterator = allPages.iterator();
		return new Iterator()
		{

			public boolean hasNext()
			{
				return allPagesIterator.hasNext();
			}

			public Object next()
			{
				RWikiObject page = (RWikiObject) allPagesIterator.next();
				return objectService.createReference(page.getName());
			}

			public void remove()
			{
				throw new UnsupportedOperationException("Remove not supported");
			}

		};
	}

	public boolean isForIndex(String reference)
	{

		try
		{
			Reference ref = getReference(reference);
			RWikiEntity rwe = (RWikiEntity) ref.getEntity();
			RWikiObject rwo = rwe.getRWikiObject();
			String pageName = rwo.getName();
			String pageSpace = NameHelper.localizeSpace(pageName, rwo.getRealm());
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

	public boolean canRead(String reference)
	{
		try
		{
			Reference ref = getReference(reference);
			RWikiEntity rwe = (RWikiEntity) ref.getEntity();
			RWikiObject rwo = rwe.getRWikiObject();
			return objectService.checkRead(rwo);
		}
		catch (Exception ex)
		{
		}
		return false;
	}

	public Map getCustomProperties(String ref)
	{
		return null;
	}

	public String getCustomRDF(String ref)
	{
		return null;
	}

	private Reference getReference(String reference)
	{
		try
		{
			Reference r = entityManager.newReference(reference);
			if (log.isDebugEnabled())
			{
				log.debug("Wiki.getReference:" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
		}
		return null;
	}

	private EntityProducer getProducer(Reference ref)
	{
		try
		{
			return ref.getEntityProducer();
		}
		catch (Exception ex)
		{
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getId(java.lang.String)
	 */
	public String getId(String reference)
	{
		try
		{
			String r = getReference(reference).getId();
			if (log.isDebugEnabled())
			{
				log.debug("Wiki.getId:" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getSubType(java.lang.String)
	 */
	public String getSubType(String reference)
	{
		try
		{
			String r = getReference(reference).getSubType();
			if (log.isDebugEnabled())
			{
				log.debug("Wiki.getSubType:" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getType(java.lang.String)
	 */
	public String getType(String reference)
	{
		try
		{
			String r = getReference(reference).getType();
			if (log.isDebugEnabled())
			{
				log.debug("Wiki.getType:" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getType(java.lang.String)
	 */
	public String getContainer(String reference)
	{
		try
		{
			String r = getReference(reference).getContainer();
			if (log.isDebugEnabled())
			{
				log.debug("Wiki.getContainer:" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

}
