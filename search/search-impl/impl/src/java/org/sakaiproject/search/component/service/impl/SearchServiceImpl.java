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

package org.sakaiproject.search.component.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchStatus;
import org.sakaiproject.search.index.IndexStorage;
import org.sakaiproject.search.model.SearchWriterLock;

/**
 * The search service
 * 
 * @author ieb
 */
public class SearchServiceImpl implements SearchService
{

	private static Log log = LogFactory.getLog(SearchServiceImpl.class);

	/**
	 * Optional dependencies
	 */
	private List triggerFunctions;


	/**
	 * the notification object
	 */
	private NotificationEdit notification = null;

	/**
	 * init completed
	 */
	private boolean initComplete = false;

	/**
	 * the currently running index searcher
	 */
	private IndexSearcher runningIndexSearcher;

	/**
	 * The index builder dependency
	 */
	private SearchIndexBuilder searchIndexBuilder;

	private long reloadStart;

	private long reloadEnd;

	private NotificationService notificationService;

	private IndexStorage indexStorage = null;

	/**
	 * Register a notification action to listen to events and modify the search
	 * index
	 */
	public void init()
	{

		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		notificationService = (NotificationService) load(cm,
				NotificationService.class.getName());
		searchIndexBuilder = (SearchIndexBuilder) load(cm,
				SearchIndexBuilder.class.getName());
		try
		{
			log.debug("init start");

			log.debug("checking setup");
			if (indexStorage == null)
			{
				log.error(" indexStorage must be set");
				throw new RuntimeException("Must set indexStorage");

			}
			if (searchIndexBuilder == null)
			{
				log.error(" searchIndexBuilder must be set");
				throw new RuntimeException("Must set searchIndexBuilder");
			}

			// register a transient notification for resources
			notification = notificationService.addTransientNotification();

			// add all the functions that are registered to trigger search index
			// modification

			notification.setFunction(SearchService.EVENT_TRIGGER_SEARCH);
			if (triggerFunctions != null)
			{
				for (Iterator ifn = triggerFunctions.iterator(); ifn.hasNext();)
				{
					String function = (String) ifn.next();
					notification.addFunction(function);
					log.debug("Adding Search Register " + function);
				}
			}

			// set the filter to any site related resource
			notification.setResourceFilter("/");

			// set the action
			notification.setAction(new SearchNotificationAction(
					searchIndexBuilder));

			// register a transient notification for resources
			NotificationEdit sbnotification = notificationService
					.addTransientNotification();

			// add all the functions that are registered to trigger search index
			// modification

			sbnotification
					.setFunction(SearchService.EVENT_TRIGGER_INDEX_RELOAD);

			// set the action
			sbnotification.setAction(new SearchReloadNotificationAction(this));

			initComplete = true;
			log.debug("init end");
		}
		catch (Throwable t)
		{
			log.error("Failed to start ", t);
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

	/**
	 * @return Returns the triggerFunctions.
	 */
	public List getTriggerFunctions()
	{
		return triggerFunctions;
	}

	/**
	 * @param triggerFunctions
	 *        The triggerFunctions to set.
	 */
	public void setTriggerFunctions(List triggerFunctions)
	{
		if (initComplete)
			throw new RuntimeException(
					" use register function at runtime, setTriggerFucntions is for Spring IoC only");
		this.triggerFunctions = triggerFunctions;
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerFunction(String function)
	{
		notification.addFunction(function);
		log.debug("Adding Function " + function);
	}

	/**
	 * {@inheritDoc}
	 */
	public SearchList search(String searchTerms, List contexts, int start,
			int end)
	{
		try
		{
			BooleanQuery query = new BooleanQuery();
			BooleanQuery contextQuery = new BooleanQuery();
			for (Iterator i = contexts.iterator(); i.hasNext();)
			{
				contextQuery.add(new TermQuery(new Term("siteid", (String) i
						.next())), true, false);
			}

			Query textQuery = QueryParser.parse(searchTerms, "contents",
					new StandardAnalyzer());
			query.add(contextQuery, true, false);
			query.add(textQuery, true, false);
			log.debug("Query is " + query.toString());
			IndexSearcher indexSearcher = getIndexSearcher(false);
			if (indexSearcher != null)
			{
				Hits h = indexSearcher.search(query);
				log.debug("Got " + h.length() + " hits");

				return new SearchListImpl(h, textQuery, start, end);
			}
			else
			{
				throw new RuntimeException(
						"Failed to start the Lucene Searche Engine");
			}

		}
		catch (ParseException e)
		{
			throw new RuntimeException("Failed to parse Query ", e);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to run Search ", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void reload()
	{
		log.debug("Reload");
		getIndexSearcher(true);

	}

	public IndexSearcher getIndexSearcher(boolean reload)
	{
		if (runningIndexSearcher == null || reload)
		{
			
			try
			{
				runningIndexSearcher = indexStorage.getIndexSearcher();
			}
			catch (IOException e)
			{
			}
		}
		return runningIndexSearcher;

	}

	public void refreshInstance()
	{
		searchIndexBuilder.refreshIndex();

	}

	public void rebuildInstance()
	{
		searchIndexBuilder.rebuildIndex();
	}

	public void refreshSite(String currentSiteId)
	{
		searchIndexBuilder.refreshIndex(currentSiteId);
	}

	public void rebuildSite(String currentSiteId)
	{
		searchIndexBuilder.rebuildIndex(currentSiteId);

	}

	public String getStatus()
	{

		String lastLoad = (new Date(reloadEnd)).toString();
		String loadTime = String
				.valueOf((double) (0.001 * (reloadEnd - reloadStart)));
		SearchWriterLock lock = searchIndexBuilder.getCurrentLock();
		List lockNodes = searchIndexBuilder.getNodeStatus();

		return "Index Last Loaded " + lastLoad + " in " + loadTime + " seconds";
	}

	public int getNDocs()
	{
		try
		{
			return getIndexSearcher(false).maxDoc();
		}
		catch (Exception e)
		{
			return -1;
		}
	}

	public int getPendingDocs()
	{
		return searchIndexBuilder.getPendingDocuments();
	}

	public List getAllSearchItems()
	{
		return searchIndexBuilder.getAllSearchItems();
	}

	public List getSiteMasterSearchItems()
	{
		return searchIndexBuilder.getSiteMasterSearchItems();
	}

	public List getGlobalMasterSearchItems()
	{
		return searchIndexBuilder.getGlobalMasterSearchItems();
	}

	public SearchStatus getSearchStatus()
	{
		final String lastLoad = (new Date(reloadEnd)).toString();
		final String loadTime = String
				.valueOf((double) (0.001 * (reloadEnd - reloadStart)));
		final SearchWriterLock lock = searchIndexBuilder.getCurrentLock();
		final List lockNodes = searchIndexBuilder.getNodeStatus();
		final String pdocs = String.valueOf(getPendingDocs());
		final String ndocs = String.valueOf(getNDocs());

		return new SearchStatus()
		{
			public String getLastLoad()
			{
				return lastLoad;
			}

			public String getLoadTime()
			{
				return loadTime;
			}

			public String getCurrentWorker()
			{
				return lock.getNodename();
			}

			public Date getCurrentWorkerETC()
			{
				return lock.getExpires();
			}

			public List getWorkerNodes()
			{
				List l = new ArrayList();
				for (Iterator i = lockNodes.iterator(); i.hasNext();)
				{
					SearchWriterLock swl = (SearchWriterLock) i.next();
					Object[] result = new Object[3];
					result[0] = swl.getNodename();
					result[1] = swl.getExpires();
					if (lock.getNodename().equals(swl.getNodename()))
					{
						result[2] = "running";
					}
					else
					{
						result[2] = "idle";
					}
					l.add(result);
				}
				return l;
			}

			public String getNDocuments()
			{
				return ndocs;
			}

			public String getPDocuments()
			{
				return pdocs;
			}

		};

	}

	public boolean removeWorkerLock()
	{
		return searchIndexBuilder.removeWorkerLock();

	}

	/**
	 * @return Returns the indexStorage.
	 */
	public IndexStorage getIndexStorage()
	{
		return indexStorage;
	}

	/**
	 * @param indexStorage The indexStorage to set.
	 */
	public void setIndexStorage(IndexStorage indexStorage)
	{
		this.indexStorage = indexStorage;
	}

}
