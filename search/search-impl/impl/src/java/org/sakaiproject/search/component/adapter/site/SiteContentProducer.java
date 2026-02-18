/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.component.adapter.site;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
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
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.util.HTMLParser;
import org.sakaiproject.search.model.SearchBuilderItem;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

/**
 * @author ieb
 */
@Slf4j
public class SiteContentProducer implements EntityContentProducer
{

	private EntityManager entityManager;

	private List<String> addEvents;

	private List<String> removeEvents;

	private SiteService siteService;

	private ServerConfigurationService serverConfigurationService;

	private SearchService searchService;

	private SearchIndexBuilder searchIndexBuilder;

	/**
	 * @return the entityManager
	 */
	public EntityManager getEntityManager()
	{
		return entityManager;
	}

	/**
	 * @param entityManager the entityManager to set
	 */
	public void setEntityManager(EntityManager entityManager)
	{
		this.entityManager = entityManager;
	}

	/**
	 * @return the searchIndexBuilder
	 */
	public SearchIndexBuilder getSearchIndexBuilder()
	{
		return searchIndexBuilder;
	}

	/**
	 * @param searchIndexBuilder the searchIndexBuilder to set
	 */
	public void setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder)
	{
		this.searchIndexBuilder = searchIndexBuilder;
	}

	/**
	 * @return the searchService
	 */
	public SearchService getSearchService()
	{
		return searchService;
	}

	/**
	 * @param searchService the searchService to set
	 */
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	/**
	 * @return the serverConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService()
	{
		return serverConfigurationService;
	}

	/**
	 * @param serverConfigurationService the serverConfigurationService to set
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	/**
	 * @return the siteService
	 */
	public SiteService getSiteService()
	{
		return siteService;
	}

	/**
	 * @param siteService the siteService to set
	 */
	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public void init()
	{
		addEvents = new ArrayList<String>();
		removeEvents = new ArrayList<String>();

		addEvents.add(SiteService.SECURE_ADD_COURSE_SITE);
		addEvents.add(SiteService.SECURE_ADD_SITE);
		addEvents.add(SiteService.SECURE_ADD_USER_SITE);
		addEvents.add(SiteService.SECURE_UPDATE_GROUP_MEMBERSHIP);
		addEvents.add(SiteService.SECURE_UPDATE_SITE);
		addEvents.add(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP);
		removeEvents.add(SiteService.SECURE_REMOVE_SITE);

		if ("true".equals(serverConfigurationService.getString("search.enable", "false")))
		{
			for (Iterator i = addEvents.iterator(); i.hasNext();)
			{
				searchService.registerFunction((String) i.next());
			}
			for (Iterator i = removeEvents.iterator(); i.hasNext();)
			{
				searchService.registerFunction((String) i.next());
			}
			searchIndexBuilder.registerEntityContentProducer(this);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#canRead(java.lang.String)
	 */
	public boolean canRead(String reference)
	{
		Reference ref = getReference(reference);
		EntityProducer ep = getProducer(ref);
		if (ep instanceof SiteService)
		{
			try
			{
				SiteService ss = (SiteService) ep;
				ss.getSite(ref.getId());
				return true;
			}
			catch (Exception ex)
			{
				log.debug(ex.getMessage());
			}
		}
		return false;
	}

	private Reference getReference(String reference)
	{
		try
		{
			Reference r = entityManager.newReference(reference);
			if (log.isDebugEnabled())
			{
				log.debug("Site.getReference" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
			log.debug(ex.getMessage());
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
			log.debug(ex.getMessage());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getAction(org.sakaiproject.event.api.Event)
	 */
	public Integer getAction(Event event)
	{
		String evt = event.getEvent();
		if (evt == null) return SearchBuilderItem.ACTION_UNKNOWN;
		for (Iterator i = addEvents.iterator(); i.hasNext();)
		{
			String match = (String) i.next();
			if (evt.equals(match))
			{
				return SearchBuilderItem.ACTION_ADD;
			}
		}
		for (Iterator i = removeEvents.iterator(); i.hasNext();)
		{
			String match = (String) i.next();
			if (evt.equals(match))
			{
				return SearchBuilderItem.ACTION_DELETE;
			}
		}
		return SearchBuilderItem.ACTION_UNKNOWN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getContainer(java.lang.String)
	 */
	public String getContainer(String ref)
	{
		// the site document is contined by itself
		Reference reference = getReference(ref);
		return reference.getId();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getContent(java.lang.String)
	 */
	public String getContent(String reference)
	{
		Reference ref = getReference(reference);
		EntityProducer ep = getProducer(ref);

		if (ep instanceof SiteService)
		{
			try
			{
				SiteService ss = (SiteService) ep;
				Site s = ss.getSite(ref.getId());
				// Only include descriptions, not the title
				// Title is indexed separately via getTitle() and FIELD_TITLE
				StringBuilder sb = new StringBuilder();
				if (s.getShortDescription() != null) {
					sb.append(s.getShortDescription());
					sb.append(" ");
				}
				if (s.getDescription() != null) {
					sb.append(s.getDescription());
				}
				return HTMLParser.stripHtml(sb.toString());

			}
			catch (IdUnusedException e)
			{
				throw new RuntimeException(" Failed to get message content ", e); //$NON-NLS-1$
			}
		}

		throw new RuntimeException(" Not a Message Entity " + reference); //$NON-NLS-1$

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getContentReader(java.lang.String)
	 */
	public Reader getContentReader(String reference)
	{
		return new StringReader(getContent(reference));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getCustomProperties(java.lang.String)
	 */
	public Map getCustomProperties(String ref)
	{

		Reference reference = getReference(ref);
		Entity e = reference.getEntity();
		ResourceProperties rp = e.getProperties();
		Map<String, String> props = new HashMap<String, String>();
		for (Iterator i = rp.getPropertyNames(); i.hasNext();)
		{
			String key = (String) i.next();
			List l = rp.getPropertyList(key);
			StringBuilder sb = new StringBuilder();
			for (Iterator is = l.iterator(); is.hasNext();)
			{
				sb.append(is.next()).append(" ");
			}
			props.put(key, sb.toString());
		}
		return props;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getCustomRDF(java.lang.String)
	 */
	public String getCustomRDF(String ref)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getId(java.lang.String)
	 */
	public String getId(String ref)
	{
		Reference reference = getReference(ref);
		return reference.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getSiteContent(java.lang.String)
	 */
	public List getSiteContent(String context)
	{
		List<String> all = new ArrayList<String>();
		try
		{
			Site s = siteService.getSite(context);
			
			all.add(s.getReference());
			return all;
		}
		catch (IdUnusedException idu)
		{
			log.debug("Site Not Found for context " + context, idu);
			return all;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getSiteContentIterator(java.lang.String)
	 */
	public Iterator getSiteContentIterator(String context)
	{
		List l = getSiteContent(context);
		return l.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getSiteId(java.lang.String)
	 */
	public String getSiteId(String ref)
	{
		// this is the site that the document is visible to, 
		// we need to look at the state of the site, and use special sites.
		// INFO: this is using not standard scoping that might want to be
		// reflected elsewhere
		Reference reference = getReference(ref);
		Entity entity = reference.getEntity();
		if (entity instanceof Site)
		{
			return entity.getId();
		}
		return null;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getSubType(java.lang.String)
	 */
	public String getSubType(String ref)
	{
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getTitle(java.lang.String)
	 */
	public String getTitle(String ref)
	{
		Reference reference = getReference(ref);
		Site s = (Site) reference.getEntity();
		return SearchUtils.appendCleanString(s.getTitle(),null).toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getTool()
	 */
	public String getTool()
	{
		return "site";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getType(java.lang.String)
	 */
	public String getType(String ref)
	{
		Reference reference = getReference(ref);
		return reference.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getUrl(java.lang.String)
	 */
	public String getUrl(String ref)
	{
		Reference reference = getReference(ref);
		return reference.getUrl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#isContentFromReader(java.lang.String)
	 */
	public boolean isContentFromReader(String reference)
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#isForIndex(java.lang.String)
	 */
	public boolean isForIndex(String ref)
	{
		Reference reference = getReference(ref);
		Site s = (Site) reference.getEntity();
		//SAK-18545 its possible the site no longer exits
		if (s != null) {
			return s.isPublished();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#matches(java.lang.String)
	 */
	public boolean matches(String ref)
	{
		Reference reference = getReference(ref);
		if (reference != null)
		{
			EntityProducer ecp = reference.getEntityProducer();
			if (ecp instanceof SiteService)
			{
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#matches(org.sakaiproject.event.api.Event)
	 */
	public boolean matches(Event event)
	{
		return addEvents.contains(event.getEvent())
				|| removeEvents.contains(event.getEvent());
	}

}
