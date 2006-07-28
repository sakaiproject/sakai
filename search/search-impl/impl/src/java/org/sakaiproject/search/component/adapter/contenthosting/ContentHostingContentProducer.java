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

package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList; 
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;

public class ContentHostingContentProducer implements EntityContentProducer
{

	private static Log log = LogFactory
			.getLog(ContentHostingContentProducer.class);

	/**
	 * resolved dep
	 */
	private SearchService searchService = null;

	/**
	 * resolved dep
	 */
	private ContentHostingService contentHostingService = null;

	/**
	 * resolved dep
	 */
	private SearchIndexBuilder searchIndexBuilder = null;

	/**
	 * resolved dep
	 */
	private EntityManager entityManager = null;

	/**
	 * resolved dep
	 */
	private SiteService siteService = null;

	/**
	 * runtime injected
	 */
	private ArrayList digesters = new ArrayList();

	/**
	 * config injected dep
	 */
	private ContentDigester defaultDigester;

	private int readerSizeLimit = 1024 * 1024 * 2; // (2M)
	
	private int digesterSizeLimit = 1024 * 1024 * 5; // (5M)

	public void init()
	{
		try
		{
			ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
					.getInstance();

			contentHostingService = (ContentHostingService) load(cm,
					ContentHostingService.class.getName());

			searchService = (SearchService) load(cm, SearchService.class
					.getName());
			searchIndexBuilder = (SearchIndexBuilder) load(cm,
					SearchIndexBuilder.class.getName());

			entityManager = (EntityManager) load(cm, EntityManager.class
					.getName());
			siteService = (SiteService) load(cm, SiteService.class.getName());

			if ("true".equals(ServerConfigurationService.getString(
					"search.experimental", "false")))
			{

				searchService
						.registerFunction(ContentHostingService.EVENT_RESOURCE_ADD);
				searchService
						.registerFunction(ContentHostingService.EVENT_RESOURCE_WRITE);
				searchService
						.registerFunction(ContentHostingService.EVENT_RESOURCE_REMOVE);
				searchIndexBuilder.registerEntityContentProducer(this);

			}

		}
		catch (Throwable t)
		{
			log.error("Failed to init Service ", t);
		}

	}

	public void addDigester(ContentDigester digester)
	{
		digesters.add(digester);
	}

	public void removeDigester(ContentDigester digester)
	{
		digesters.remove(digester);
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
		ContentResource contentResource;
		try
		{
			contentResource = contentHostingService.getResource(cr.getId());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to resolve resource "+cr, e);
		}
		if (contentResource.getContentLength() > readerSizeLimit) return true;
		return false;
	}

	public Reader getContentReader(Entity cr)
	{
		ContentResource contentResource;
		try
		{
			contentResource = contentHostingService.getResource(cr.getId());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to resolve resource "+cr, e);
		}
		if ( contentResource.getContentLength() <= 0 ) {
			return new StringReader("");
		}		
		
		ContentDigester digester = getDigester(contentResource);
		Reader reader = null;
		try
		{
			reader = digester.getContentReader(contentResource);
		}
		catch (Exception ex)
		{
			log.debug("Failed to generate content with " + digester, ex);
			if (!digester.equals(defaultDigester))
			{
				try
				{
					reader = defaultDigester.getContentReader(contentResource);
				}
				catch (Exception ex2)
				{
					log.debug("Failed to extract content from "
							+ contentResource + " using " + defaultDigester,
							ex2);
					throw new RuntimeException(
							"Failed to extract content from " + contentResource
									+ " using " + defaultDigester + " and "
									+ digester, ex);
				}
			}
			else
			{
				throw new RuntimeException("Failed to extract content from "
						+ contentResource + " using " + digester, ex);
			}
		}
		return reader;
	}

	public String getContent(Entity cr)
	{
		ContentResource contentResource;
		try
		{
			contentResource = contentHostingService.getResource(cr.getId());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to resolve resource ", e);
		}
		if ( contentResource.getContentLength() <= 0 ) {
			return "";
		}
		ContentDigester digester = getDigester(contentResource);
		String content = null;
		try
		{
			content = digester.getContent(contentResource);
		}
		catch (Exception ex)
		{
			log.debug("Failed to generate content with " + digester, ex);
			if (!digester.equals(defaultDigester))
			{
				try
				{
					content = defaultDigester.getContent(contentResource);
				}
				catch (Exception ex2)
				{
					log.debug("Failed to extract content from "
							+ contentResource + " using " + defaultDigester,
							ex2);
					throw new RuntimeException(
							"Failed to extract content from " + contentResource
									+ " using " + defaultDigester + " and "
									+ digester, ex);
				}
			}
			else
			{
				throw new RuntimeException("Failed to extract content from "
						+ contentResource + " using " + digester, ex);
			}
		}
		return content;

	}

	public ContentDigester getDigester(ContentResource cr)
	{
		if ( cr.getContentLength() > digesterSizeLimit ) {
			return defaultDigester;
		}
		String mimeType = cr.getContentType();
		for (Iterator i = digesters.iterator(); i.hasNext();)
		{
			ContentDigester digester = (ContentDigester) i.next();
			if (digester.accept(mimeType))
			{
				return digester;
			}
		}
		return defaultDigester;

	}

	public String getTitle(Entity cr)
	{
		ContentResource contentResource;
		try
		{
			contentResource = contentHostingService.getResource(cr.getId());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to resolve resource ", e);
		}
		ResourceProperties rp = contentResource.getProperties();
		String displayNameProp = rp.getNamePropDisplayName();
		return rp.getProperty(displayNameProp);
	}

	public boolean matches(Reference ref)
	{
		EntityProducer ep = ref.getEntityProducer();
		return (ep instanceof ContentHostingService);
	}

	public List getAllContent()
	{
		List sites = siteService.getSites(SelectionType.ANY, null, null, null,
				SortType.NONE, null);
		List l = new ArrayList();
		for (Iterator is = sites.iterator(); is.hasNext();)
		{
			Site s = (Site) is.next();
			String siteCollection = contentHostingService.getSiteCollection(s
					.getId());
			List siteContent = contentHostingService
					.getAllResources(siteCollection);
			for (Iterator i = siteContent.iterator(); i.hasNext();)
			{
				ContentResource resource = (ContentResource) i.next();
				l.add(resource.getReference());
			}
		}
		return l;

	}

	public Integer getAction(Event event)
	{
		String eventName = event.getEvent();
		if (ContentHostingService.EVENT_RESOURCE_ADD.equals(eventName)
				|| ContentHostingService.EVENT_RESOURCE_WRITE.equals(eventName))
		{
			return SearchBuilderItem.ACTION_ADD;
		}
		if (ContentHostingService.EVENT_RESOURCE_REMOVE.equals(eventName))
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
		return "content";
	}

	public String getUrl(Entity entity)
	{
		return entity.getUrl();
	}

	public String getSiteId(Reference ref)
	{
		return ref.getContext();
	}

	public String getSiteId(String resourceName)
	{
		return getSiteId(entityManager.newReference(resourceName));
	}

	public List getSiteContent(String context)
	{
		String siteCollection = contentHostingService
				.getSiteCollection(context);
		List siteContent = contentHostingService
				.getAllResources(siteCollection);
		List l = new ArrayList();
		for (Iterator i = siteContent.iterator(); i.hasNext();)
		{
			ContentResource resource = (ContentResource) i.next();
			l.add(resource.getReference());
		}
		return l;
	}

	/**
	 * @return Returns the readerSizeLimit.
	 */
	public int getReaderSizeLimit()
	{
		return readerSizeLimit;
	}

	/**
	 * @param readerSizeLimit
	 *        The readerSizeLimit to set.
	 */
	public void setReaderSizeLimit(int readerSizeLimit)
	{
		this.readerSizeLimit = readerSizeLimit;
	}

	/**
	 * @return Returns the defaultDigester.
	 */
	public ContentDigester getDefaultDigester()
	{
		return defaultDigester;
	}

	/**
	 * @param defaultDigester
	 *        The defaultDigester to set.
	 */
	public void setDefaultDigester(ContentDigester defaultDigester)
	{
		this.defaultDigester = defaultDigester;
	}

	public boolean isForIndex(Reference ref)
	{
		ContentResource contentResource;
		try
		{
			contentResource = contentHostingService.getResource(ref.getId());
		}
		catch (IdUnusedException idun)
		{
			return false; // a collection or unknown resource that cant be
			// indexed
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to resolve resource ", e);
		}
		return true;
	}

	public boolean canRead(Reference ref)
	{
		try {
			contentHostingService.checkResource(ref.getId());
			return true;
		} catch ( Exception ex ) {
			return false;
		}
	}

	public Map getCustomProperties()
	{
		return null;
	}

	public String getCustomRDF()
	{
		return null;
	}

	/**
	 * @return Returns the digesterSizeLimit.
	 */
	public int getDigesterSizeLimit()
	{
		return digesterSizeLimit;
	}

	/**
	 * @param digesterSizeLimit The digesterSizeLimit to set.
	 */
	public void setDigesterSizeLimit(int digesterSizeLimit)
	{
		this.digesterSizeLimit = digesterSizeLimit;
	}



}
