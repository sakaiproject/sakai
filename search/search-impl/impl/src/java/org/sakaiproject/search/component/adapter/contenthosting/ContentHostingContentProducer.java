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

	private static Log log = LogFactory.getLog(ContentHostingContentProducer.class);

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

			searchService = (SearchService) load(cm, SearchService.class.getName());
			searchIndexBuilder = (SearchIndexBuilder) load(cm, SearchIndexBuilder.class
					.getName());

			entityManager = (EntityManager) load(cm, EntityManager.class.getName());
			siteService = (SiteService) load(cm, SiteService.class.getName());

			if ("true".equals(ServerConfigurationService.getString("search.enable",
					"false")))
			{

				searchService.registerFunction(ContentHostingService.EVENT_RESOURCE_ADD);
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

	public boolean isContentFromReader(String ref)
	{
		boolean debug = log.isDebugEnabled();
		ContentResource contentResource;
		try
		{
			Reference reference = entityManager.newReference(ref);
			contentResource = contentHostingService.getResource(reference.getId());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to resolve resource " + ref, e);
		}
		if (contentResource.getContentLength() > readerSizeLimit)
		{
			if (debug)
			{
				log.debug("ContentHosting.isContentFromReader" + ref + ":yes");
			}
			return true;
		}
		if (debug)
		{
			log.debug("ContentHosting.isContentFromReader" + ref + ":yes");
		}
		return false;
	}

	public Reader getContentReader(String ref)
	{
		boolean debug = log.isDebugEnabled();
		ContentResource contentResource;
		try
		{
			Reference reference = entityManager.newReference(ref);
			contentResource = contentHostingService.getResource(reference.getId());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to resolve resource " + ref, e);
		}
		if (contentResource.getContentLength() <= 0)
		{
			if (debug)
			{
				log.debug("ContentHosting.getContentReader" + ref + ": empty");
			}
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
					log.debug("Failed to extract content from " + contentResource
							+ " using " + defaultDigester, ex2);
					throw new RuntimeException("Failed to extract content from "
							+ contentResource + " using " + defaultDigester + " and "
							+ digester, ex);
				}
			}
			else
			{
				throw new RuntimeException("Failed to extract content from "
						+ contentResource + " using " + digester, ex);
			}
		}
		if (debug)
		{
			log.debug("ContentHosting.getContentReader" + ref + ":" + reader);
		}
		return reader;
	}

	public String getContent(String ref)
	{
		return getContent(ref, 3);
	}

	public String getContent(String ref, int minWordLenght)
	{
		boolean debug = log.isDebugEnabled();
		ContentResource contentResource;
		try
		{
			Reference reference = entityManager.newReference(ref);
			contentResource = contentHostingService.getResource(reference.getId());
		}
		catch (Exception e)
		{
			if (debug)
			{
				log.debug("Failed To resolve Resource", e);
			}
			throw new RuntimeException("Failed to resolve resource ", e);
		}
		if (contentResource.getContentLength() <= 0)
		{
			if (debug)
			{
				log.debug("ContentHosting.getContent" + ref + ":empty");
			}
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
					log.debug("Failed to extract content from " + contentResource
							+ " using " + defaultDigester, ex2);
					throw new RuntimeException("Failed to extract content from "
							+ contentResource + " using " + defaultDigester + " and "
							+ digester, ex);
				}
			}
			else
			{
				if (debug)
				{
					log.debug("Failed To extract content");
				}
				throw new RuntimeException("Failed to extract content from "
						+ contentResource + " using " + digester, ex);
			}
		}
		if (debug)
		{
			log.debug("ContentHosting.getContent" + ref + ":" + content);
		}
		return content;

	}

	public ContentDigester getDigester(ContentResource cr)
	{
		boolean debug = log.isDebugEnabled();
		if (cr.getContentLength() > digesterSizeLimit)
		{
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

	public String getTitle(String ref)
	{
		boolean debug = log.isDebugEnabled();
		ContentResource contentResource;
		try
		{
			Reference reference = entityManager.newReference(ref);
			contentResource = contentHostingService.getResource(reference.getId());
		}
		catch (Exception e)
		{
			if (debug)
			{
				log.debug("Failed To resolve Resource", e);
			}

			throw new RuntimeException("Failed to resolve resource ", e);
		}
		ResourceProperties rp = contentResource.getProperties();
		String displayNameProp = rp.getNamePropDisplayName();
		String title = rp.getProperty(displayNameProp);
		if (debug)
		{
			log.debug("ContentHosting.getTitle" + ref + ":" + title);
		}
		return title;
	}

	public boolean matches(String ref)
	{
		boolean debug = log.isDebugEnabled();
		try
		{
			Reference reference = entityManager.newReference(ref);
			EntityProducer ep = reference.getEntityProducer();
			boolean m = (ep instanceof ContentHostingService);
			if (debug)
			{
				log.debug("ContentHosting.matches" + ref + ":" + m);
			}
			return m;

		}
		catch (Exception ex)
		{
			if (debug)
			{
				log.debug("ContentHosting.matches" + ref + ":fail-no-match");
			}
			return false;
		}
	}

	public List getAllContent()
	{
		boolean debug = log.isDebugEnabled();
		List sites = siteService.getSites(SelectionType.ANY, null, null, null,
				SortType.NONE, null);
		List l = new ArrayList();
		for (Iterator is = sites.iterator(); is.hasNext();)
		{
			Site s = (Site) is.next();
			String siteCollection = contentHostingService.getSiteCollection(s.getId());
			List siteContent = contentHostingService.getAllResources(siteCollection);
			for (Iterator i = siteContent.iterator(); i.hasNext();)
			{
				ContentResource resource = (ContentResource) i.next();
				l.add(resource.getReference());
			}
		}
		if (debug)
		{
			log.debug("ContentHosting.getAllContent::" + l.size());
		}
		return l;

	}

	public Integer getAction(Event event)
	{
		boolean debug = log.isDebugEnabled();
		String eventName = event.getEvent();
		if (ContentHostingService.EVENT_RESOURCE_ADD.equals(eventName)
				|| ContentHostingService.EVENT_RESOURCE_WRITE.equals(eventName))
		{
			if (debug)
			{
				log.debug("ContentHosting.getAction" + event + ":add");
			}
			if ( isForIndex(event.getResource())) {
				return SearchBuilderItem.ACTION_ADD;
			}
		}
		if (ContentHostingService.EVENT_RESOURCE_REMOVE.equals(eventName))
		{
			if (debug)
			{
				log.debug("ContentHosting.getAction" + event + ":delete");
			}
			if ( isForIndex(event.getResource())) {
				return SearchBuilderItem.ACTION_DELETE;
			}
		}
		if (debug)
		{
			log.debug("ContentHosting.getAction" + event + ":uknown");
		}
		return SearchBuilderItem.ACTION_UNKNOWN;
	}

	public boolean matches(Event event)
	{
		boolean debug = log.isDebugEnabled();
		boolean m = !SearchBuilderItem.ACTION_UNKNOWN.equals(getAction(event));
		if (debug)
		{
			log.debug("ContentHosting.matches" + event + ":" + m);
		}
		return m;
	}

	public String getTool()
	{
		return "content";
	}

	public String getUrl(String ref)
	{
		boolean debug = log.isDebugEnabled();
		Reference reference = entityManager.newReference(ref);
		String url = reference.getUrl();
		if (debug)
		{
			log.debug("ContentHosting.getAction" + ref + ":" + url);
		}
		return url;
	}

	private String getSiteId(Reference ref)
	{
		String r = ref.getContext();
		if (log.isDebugEnabled())
		{
			log.debug("ContentHosting.getSiteId" + ref + ":" + r);
		}
		return r;
	}

	public String getSiteId(String resourceName)
	{
		String r = getSiteId(entityManager.newReference(resourceName));
		if (log.isDebugEnabled())
		{
			log.debug("ContentHosting.getSiteId" + resourceName + ":" + r);
		}
		return r;
	}

	public List getSiteContent(String context)
	{
		boolean debug = log.isDebugEnabled();
		String siteCollection = contentHostingService.getSiteCollection(context);
		List siteContent = contentHostingService.getAllResources(siteCollection);
		List l = new ArrayList();
		for (Iterator i = siteContent.iterator(); i.hasNext();)
		{
			ContentResource resource = (ContentResource) i.next();
			l.add(resource.getReference());
		}
		if (debug)
		{
			log.debug("ContentHosting.getSiteContent" + context + ":" + l.size());
		}
		return l;
	}

	public Iterator getSiteContentIterator(String context)
	{
		boolean debug = log.isDebugEnabled();

		String siteCollection = contentHostingService.getSiteCollection(context);
		if (debug)
		{
			log.debug("Getting content for site info " + siteCollection);
		}
		List siteContent = null;
		if ("/".equals(siteCollection))
		{
			siteContent = new ArrayList();
		}
		else
		{
			siteContent = contentHostingService.getAllResources(siteCollection);
		}
		final Iterator scIterator = siteContent.iterator();
		return new Iterator()
		{

			public boolean hasNext()
			{
				return scIterator.hasNext();
			}

			public Object next()
			{
				ContentResource resource = (ContentResource) scIterator.next();
				return resource.getReference();
			}

			public void remove()
			{
				throw new UnsupportedOperationException("Remove is not implimented ");
			}

		};
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

	public boolean isForIndex(String ref)
	{
		ContentResource contentResource;
		try
		{
			// nasty hack to not index dropbox without loading an entity from the DB
			if ( ref.length() > "/content".length() && contentHostingService.isInDropbox(ref.substring("/content".length())) ) {
					return false;
			}
			
			Reference reference = entityManager.newReference(ref);
			

			contentResource = contentHostingService.getResource(reference.getId());
			if (contentResource == null || contentResource.isCollection() )
			{
				return false;
			}
		}
		catch (IdUnusedException idun)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Resource Not present in CHS " + ref);
			}

			return false; // a collection or unknown resource that cant be
			// indexed
		}
		catch (Exception e)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Failed To resolve Resource", e);
			}
			throw new RuntimeException("Failed to resolve resource ", e);
		}
		return true;
	}

	public boolean canRead(String ref)
	{
		try
		{
			Reference reference = entityManager.newReference(ref);
			contentHostingService.checkResource(reference.getId());
			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	public Map getCustomProperties(String ref)
	{
		
		
		
		
		
		return null;
	}

	public String getCustomRDF(String ref)
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
	 * @param digesterSizeLimit
	 *        The digesterSizeLimit to set.
	 */
	public void setDigesterSizeLimit(int digesterSizeLimit)
	{
		this.digesterSizeLimit = digesterSizeLimit;
	}

	private Reference getReference(String reference)
	{
		try
		{
			return entityManager.newReference(reference);
		}
		catch (Exception ex)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Failed To resolve Resource", ex);
			}

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
		boolean debug = log.isDebugEnabled();
		try
		{
			return getReference(reference).getId();
		}
		catch (Exception ex)
		{
			if (debug)
			{
				log.debug("Failed To resolve Resource", ex);
			}

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
		boolean debug = log.isDebugEnabled();
		try
		{
			String r = getReference(reference).getSubType();
			if (debug)
			{
				log.debug("ContentHosting.getSubType" + reference + ":" + r);
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
		boolean debug = log.isDebugEnabled();
		try
		{
			String r = getReference(reference).getType();
			if (debug)
			{
				log.debug("ContentHosting.getType" + reference + ":" + r);
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
		boolean debug = log.isDebugEnabled();
		try
		{
			String r = getReference(reference).getContainer();
			if (debug)
			{
				log.debug("ContentHosting.getContainer" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

}
