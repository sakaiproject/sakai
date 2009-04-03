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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.component.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.dao.SearchBuilderItemDao;
import org.sakaiproject.search.indexer.api.IndexQueueListener;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;

/**
 * Search index builder is expected to be registered in spring as
 * org.sakaiproject.search.api.SearchIndexBuilder as a singleton. It receives
 * resources which it adds to its list of pending documents to be indexed. A
 * seperate thread then runs thtough the list of entities to be indexed,
 * updating the index. Each time the index is updates an event is posted to
 * force the Search components that are using the index to reload. Incremental
 * updates to the Lucene index require that the searchers reload the index once
 * the idex writer has been built.
 * 
 * @author ieb
 */

public class SearchIndexBuilderImpl implements SearchIndexBuilder
{

	private static Log log = LogFactory.getLog(SearchIndexBuilderImpl.class);

	/**
	 * dependency
	 */
	private SearchBuilderItemDao searchBuilderItemDao = null;

	private List producers = new ArrayList();

	private boolean onlyIndexSearchToolSites = false;

	private List<IndexQueueListener> indexQueueListeners = new ArrayList<IndexQueueListener>();

	private boolean excludeUserSites = true;

	public void init()
	{
	}


	/**
	 * register an entity content producer to provide content to the search
	 * engine {@inheritDoc}
	 */
	public void registerEntityContentProducer(EntityContentProducer ecp)
	{
		log.debug("register " + ecp);
		producers.add(ecp);
	}

	/**
	 * Add a resource to the indexing queue {@inheritDoc}
	 */
	public void addResource(Notification notification, Event event)
	{
		log.debug("Add resource " + notification + "::" + event);
		String resourceName = event.getResource();
		if (resourceName == null)
		{
			// default if null
			resourceName = "";
		}
		if (resourceName.length() > 255)
		{
			log
					.warn("Entity Reference is longer than 255 characters, not indexing. Reference="
							+ resourceName);
			return;
		}
		EntityContentProducer ecp = newEntityContentProducer(event);
		if ( ecp == null || ecp.getSiteId(resourceName) == null)
		{
			log.debug("Not indexing " + resourceName + " as it has no context");
			return;
		}
		if (onlyIndexSearchToolSites)
		{
			try
			{
				String siteId = ecp.getSiteId(resourceName);
				Site s = SiteService.getSite(siteId);
				ToolConfiguration t = s.getToolForCommonId("sakai.search");
				if (t == null)
				{
					log.debug("Not indexing " + resourceName
							+ " as it has no search tool");
					return;
				}
			}
			catch (Exception ex)
			{
				log.debug("Not indexing  " + resourceName + " as it has no site", ex);
				return;

			}
		}
		Integer action = ecp.getAction(event);
		try
		{
			SearchBuilderItem sb = searchBuilderItemDao.findByName(resourceName);
			if (sb == null)
			{
				// new
				sb = searchBuilderItemDao.create();
				sb.setSearchaction(action);
				sb.setName(resourceName);
				String siteId = ecp.getSiteId(resourceName);
				if (siteId == null || siteId.length() == 0)
				{
					// default if null should neve happen
					siteId = "none";
				}
				sb.setContext(siteId);
				sb.setSearchstate(SearchBuilderItem.STATE_PENDING);
				sb.setItemscope(SearchBuilderItem.ITEM);

			}
			else
			{
				sb.setSearchaction(action);
				String siteId = ecp.getSiteId(resourceName);
				if (siteId == null || siteId.length() == 0)
				{
					// default if null, should never happen
					siteId = "none";
				}
				sb.setContext(siteId);
				sb.setName(resourceName);
				sb.setSearchstate(SearchBuilderItem.STATE_PENDING);
				sb.setItemscope(SearchBuilderItem.ITEM);
			}
			searchBuilderItemDao.update(sb);
			log.debug("SEARCHBUILDER: Added Resource " + action + " " + sb.getName());
			fireResourceAdded(resourceName);
		}
		catch (Throwable t)
		{
			log.debug("In trying to register resource " + resourceName
					+ " in search engine this resource will"
					+ " not be indexed untill it is modified");
		}
	}

	protected void fireResourceAdded(String name) 
	{
		for (Iterator<IndexQueueListener> itl = indexQueueListeners.iterator(); itl
				.hasNext();)
		{
			IndexQueueListener tl = itl.next();
			tl.added(name);
		}
	}

	public void addIndexQueueListener(IndexQueueListener indexQueueListener)
	{
		List<IndexQueueListener> tl = new ArrayList<IndexQueueListener>();
		tl.addAll(indexQueueListeners);
		tl.add(indexQueueListener);
		indexQueueListeners = tl;
	}

	public void removeIndexQueueListener(IndexQueueListener indexQueueListener)
	{
		List<IndexQueueListener> tl = new ArrayList<IndexQueueListener>();
		tl.addAll(indexQueueListeners);
		tl.remove(indexQueueListener);
		indexQueueListeners = tl;
	}

	
	
	/**
	 * refresh the index from the current stored state {@inheritDoc}
	 */
	public void refreshIndex()
	{

		SearchBuilderItem sb = searchBuilderItemDao
				.findByName(SearchBuilderItem.GLOBAL_MASTER);
		if (sb == null)
		{
			log.debug("Created NEW " + SearchBuilderItem.GLOBAL_MASTER);
			sb = searchBuilderItemDao.create();
		}

		if ((SearchBuilderItem.STATE_COMPLETED.equals(sb.getSearchstate()))
				|| (!SearchBuilderItem.ACTION_REBUILD.equals(sb.getSearchaction())
						&& !SearchBuilderItem.STATE_PENDING.equals(sb.getSearchstate()) && !SearchBuilderItem.STATE_PENDING_2
						.equals(sb.getSearchstate())))
		{
			sb.setSearchaction(SearchBuilderItem.ACTION_REFRESH);
			sb.setName(SearchBuilderItem.GLOBAL_MASTER);
			sb.setContext(SearchBuilderItem.GLOBAL_CONTEXT);
			sb.setSearchstate(SearchBuilderItem.STATE_PENDING);
			sb.setItemscope(SearchBuilderItem.ITEM_GLOBAL_MASTER);
			searchBuilderItemDao.update(sb);
			log.debug("SEARCHBUILDER: REFRESH ALL " + sb.getSearchaction() + " "
					+ sb.getName());
			fireResourceAdded(String.valueOf(SearchBuilderItem.GLOBAL_MASTER));
		}
		else
		{
			log.debug("SEARCHBUILDER: REFRESH ALL IN PROGRESS " + sb.getSearchaction()
					+ " " + sb.getName());
		}
	}

	public void destroy()
	{
	}

	/*
	 * List l = searchBuilderItemDao.getAll(); for (Iterator i = l.iterator();
	 * i.hasNext();) { SearchBuilderItemImpl sbi = (SearchBuilderItemImpl)
	 * i.next(); sbi.setSearchstate(SearchBuilderItem.STATE_PENDING);
	 * sbi.setSearchaction(SearchBuilderItem.ACTION_ADD); try { log.info("
	 * Updating " + sbi.getName()); searchBuilderItemDao.update(sbi); } catch
	 * (Exception ex) { try { sbi = (SearchBuilderItemImpl) searchBuilderItemDao
	 * .findByName(sbi.getName()); if (sbi != null) {
	 * sbi.setSearchstate(SearchBuilderItem.STATE_PENDING);
	 * sbi.setSearchaction(SearchBuilderItem.ACTION_ADD);
	 * searchBuilderItemDao.update(sbi); } } catch (Exception e) {
	 * log.warn("Failed to update on second attempt " + e.getMessage()); } } }
	 * restartBuilder(); }
	 */
	/**
	 * Rebuild the index from the entities own stored state {@inheritDoc}
	 */
	public void rebuildIndex()
	{

		try
		{
			SearchBuilderItem sb = searchBuilderItemDao
					.findByName(SearchBuilderItem.GLOBAL_MASTER);
			if (sb == null)
			{
				sb = searchBuilderItemDao.create();
			}
			sb.setSearchaction(SearchBuilderItem.ACTION_REBUILD);
			sb.setName(SearchBuilderItem.GLOBAL_MASTER);
			sb.setContext(SearchBuilderItem.GLOBAL_CONTEXT);
			sb.setSearchstate(SearchBuilderItem.STATE_PENDING);
			sb.setItemscope(SearchBuilderItem.ITEM_GLOBAL_MASTER);
			searchBuilderItemDao.update(sb);
			log.debug("SEARCHBUILDER: REBUILD ALL " + sb.getSearchaction() + " "
					+ sb.getName());
			fireResourceAdded(String.valueOf(SearchBuilderItem.GLOBAL_MASTER));
		}
		catch (Exception ex)
		{
			log.warn(" rebuild index encountered a problme " + ex.getMessage());
		}
	}

	/*
	 * for (Iterator i = producers.iterator(); i.hasNext();) {
	 * EntityContentProducer ecp = (EntityContentProducer) i.next(); List
	 * contentList = ecp.getAllContent(); for (Iterator ci =
	 * contentList.iterator(); ci.hasNext();) { String resourceName = (String)
	 * ci.next(); } } }
	 */

	/**
	 * Generates a SearchableEntityProducer
	 * 
	 * @param ref
	 * @return
	 * @throws PermissionException
	 * @throws IdUnusedException
	 * @throws TypeException
	 */
	public EntityContentProducer newEntityContentProducer(String ref)
	{
		log.debug(" new entitycontent producer");
		for (Iterator i = producers.iterator(); i.hasNext();)
		{
			EntityContentProducer ecp = (EntityContentProducer) i.next();
			if (ecp.matches(ref))
			{
				return ecp;
			}
		}
		return null;
	}

	/**
	 * get hold of an entity content producer using the event
	 * 
	 * @param event
	 * @return
	 */
	public EntityContentProducer newEntityContentProducer(Event event)
	{
		log.debug(" new entitycontent producer");
		for (Iterator i = producers.iterator(); i.hasNext();)
		{
			EntityContentProducer ecp = (EntityContentProducer) i.next();
			if (ecp.matches(event))
			{
				log.debug(" Matched Entity Content Producer for event " + event
						+ " with " + ecp);
				return ecp;
			}
			else
			{
				log.debug("Skipped ECP " + ecp);
			}
		}
		log.debug("Failed to match any Entity Content Producer for event " + event);
		return null;
	}

	/**
	 * @return Returns the searchBuilderItemDao.
	 */
	public SearchBuilderItemDao getSearchBuilderItemDao()
	{
		return searchBuilderItemDao;
	}

	/**
	 * @param searchBuilderItemDao
	 *        The searchBuilderItemDao to set.
	 */
	public void setSearchBuilderItemDao(SearchBuilderItemDao searchBuilderItemDao)
	{
		this.searchBuilderItemDao = searchBuilderItemDao;
	}

	/**
	 * return true if the queue is empty
	 * 
	 * @{inheritDoc}
	 */
	public boolean isBuildQueueEmpty()
	{
		int n = searchBuilderItemDao.countPending();
		log.debug("Queue has " + n);
		return (n == 0);
	}

	/**
	 * get all the producers registerd, as a clone to avoid concurrent
	 * modification exceptions
	 * 
	 * @return
	 */
	public List getContentProducers()
	{
		return new ArrayList(producers);
	}

	public int getPendingDocuments()
	{
		return searchBuilderItemDao.countPending();
	}

	/**
	 * Rebuild the index from the entities own stored state {@inheritDoc}, just
	 * the supplied siteId
	 */
	public void rebuildIndex(String currentSiteId)
	{

		try
		{
			if (currentSiteId == null || currentSiteId.length() == 0)
			{
				currentSiteId = "none";
			}
			String siteMaster = MessageFormat.format(
					SearchBuilderItem.SITE_MASTER_FORMAT, new Object[] { currentSiteId });
			SearchBuilderItem sb = searchBuilderItemDao.findByName(siteMaster);
			if (sb == null)
			{
				sb = searchBuilderItemDao.create();
			}
			sb.setSearchaction(SearchBuilderItem.ACTION_REBUILD);
			sb.setName(siteMaster);
			sb.setContext(currentSiteId);
			sb.setSearchstate(SearchBuilderItem.STATE_PENDING);
			sb.setItemscope(SearchBuilderItem.ITEM_SITE_MASTER);
			searchBuilderItemDao.update(sb);
			log.debug("SEARCHBUILDER: REBUILD CONTEXT " + sb.getSearchaction() + " "
					+ sb.getName());
			fireResourceAdded(sb.getName());
		}
		catch (Exception ex)
		{
			log.warn(" rebuild index encountered a problme " + ex.getMessage());
		}
	}

	/**
	 * Refresh the index fo the supplied site.
	 */
	public void refreshIndex(String currentSiteId)
	{
		if (currentSiteId == null || currentSiteId.length() == 0)
		{
			currentSiteId = "none";
		}
		String siteMaster = MessageFormat.format(SearchBuilderItem.SITE_MASTER_FORMAT,
				new Object[] { currentSiteId });
		SearchBuilderItem sb = searchBuilderItemDao.findByName(siteMaster);
		if (sb == null)
		{
			log.debug("Created NEW " + siteMaster);
			sb = searchBuilderItemDao.create();
			sb.setContext(currentSiteId);
			sb.setName(siteMaster);
			sb.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
			sb.setSearchaction(SearchBuilderItem.ACTION_REFRESH);
			sb.setItemscope(SearchBuilderItem.ITEM_SITE_MASTER);
		}
		if ((SearchBuilderItem.STATE_COMPLETED.equals(sb.getSearchstate()))
				|| (!SearchBuilderItem.ACTION_REBUILD.equals(sb.getSearchaction())
						&& !SearchBuilderItem.STATE_PENDING.equals(sb.getSearchstate()) && !SearchBuilderItem.STATE_PENDING_2
						.equals(sb.getSearchstate())))
		{
			sb.setSearchaction(SearchBuilderItem.ACTION_REFRESH);
			sb.setName(siteMaster);
			sb.setContext(currentSiteId);
			sb.setSearchstate(SearchBuilderItem.STATE_PENDING);
			sb.setItemscope(SearchBuilderItem.ITEM_SITE_MASTER);
			searchBuilderItemDao.update(sb);
			log.debug("SEARCHBUILDER: REFRESH CONTEXT " + sb.getSearchaction() + " "
					+ sb.getName());
			fireResourceAdded(sb.getName());
		}
		else
		{
			log.debug("SEARCHBUILDER: REFRESH CONTEXT IN PROGRESS "
					+ sb.getSearchaction() + " " + sb.getName());
		}
	}

	public List getAllSearchItems()
	{

		return searchBuilderItemDao.getAll();
	}

	public List getGlobalMasterSearchItems()
	{
		return searchBuilderItemDao.getGlobalMasters();
	}

	public List getSiteMasterSearchItems()
	{
		return searchBuilderItemDao.getSiteMasters();
	}

	/*
	public SearchWriterLock getCurrentLock()
	{
		return searchIndexBuilderWorker.getCurrentLock();
	}

	public List getNodeStatus()
	{
		return searchIndexBuilderWorker.getNodeStatus();
	}

	public boolean removeWorkerLock()
	{
		return searchIndexBuilderWorker.removeWorkerLock();
	}

	public String getLastDocument()
	{
		return searchIndexBuilderWorker.getLastDocument();
	}

	public String getLastElapsed()
	{
		return searchIndexBuilderWorker.getLastElapsed();
	}

	public String getCurrentDocument()
	{
		return searchIndexBuilderWorker.getCurrentDocument();
	}

	public String getCurrentElapsed()
	{
		return searchIndexBuilderWorker.getCurrentElapsed();
	}
	*/

	/**
	 * @return the onlyIndexSearchToolSites
	 */
	public boolean isOnlyIndexSearchToolSites()
	{
		return onlyIndexSearchToolSites;
	}

	/**
	 * @param onlyIndexSearchToolSites
	 *        the onlyIndexSearchToolSites to set
	 */
	public void setOnlyIndexSearchToolSites(boolean onlyIndexSearchToolSites)
	{
		this.onlyIndexSearchToolSites = onlyIndexSearchToolSites;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#isLocalLock()
	 */
	/*
	public boolean isLocalLock()
	{
		return searchIndexBuilderWorker.isLocalLock();
	}
	*/
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#disableDiagnostics()
	 */
	/*
	public void disableDiagnostics()
	{
		diagnostics = false;
		searchIndexBuilderWorker.enableDiagnostics();
	}
	*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#enableDiagnostics()
	 */
	/*
	public void enableDiagnostics()
	{
		diagnostics = true;
		searchIndexBuilderWorker.disableDiagnostics();
	}
	*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#hasDiagnostics()
	 */
	/*
	public boolean hasDiagnostics()
	{
		return diagnostics;
	}
	*/
	
	
	public void setExcludeUserSites(boolean excludeUserSites)
	{
		this.excludeUserSites  = excludeUserSites;
	}

	public boolean isExcludeUserSites()
	{
		// TODO Auto-generated method stub
		return excludeUserSites;
	}

}
