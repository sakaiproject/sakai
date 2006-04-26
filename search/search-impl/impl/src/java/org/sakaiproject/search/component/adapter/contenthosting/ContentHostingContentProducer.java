/*******************************************************************************
 * $Header$
 * **********************************************************************************
 * Copyright (c) 2006 University of Cambridge Licensed under the Educational
 * Community License Version 1.0 (the "License"); By obtaining, using and/or
 * copying this Original Work, you agree that you have read, understand, and
 * will comply with the terms and conditions of the Educational Community
 * License. You may obtain a copy of the License at:
 * http://cvs.sakaiproject.org/licenses/license_1_0.html THE SOFTWARE IS
 * PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/

package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.sakaiproject.event.api.Event;
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

	private int readerSizeLimit = 1024 * 1024 * 200; // (200K)

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

			if ("true".equals(ServerConfigurationService
					.getString("search.experimental")))
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
			throw new RuntimeException("Failed to resolve resource ", e);
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
			throw new RuntimeException("Failed to resolve resource ", e);
		}
		return getDigester(contentResource).getContentReader(contentResource);
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
		return getDigester(contentResource).getContent(contentResource);

	}

	public ContentDigester getDigester(ContentResource cr)
	{
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
		return contentResource.getProperties().getNamePropDisplayName();
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
		return "Content";
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
	 * @param defaultDigester The defaultDigester to set.
	 */
	public void setDefaultDigester(ContentDigester defaultDigester)
	{
		this.defaultDigester = defaultDigester;
	}

}
