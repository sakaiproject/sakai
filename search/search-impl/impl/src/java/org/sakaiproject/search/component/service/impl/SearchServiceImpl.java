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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.search.api.SearchIndexBuilderWorker;
import org.sakaiproject.search.api.SearchStatus;
import org.sakaiproject.search.component.Messages;
import org.sakaiproject.search.index.IndexStorage;
import org.sakaiproject.search.model.SearchWriterLock;

/**
 * The search service
 * 
 * @author ieb
 */
public class SearchServiceImpl extends BaseSearchServiceImpl
{

	private static Log log = LogFactory.getLog(SearchServiceImpl.class);

	/**
	 * The index builder dependency
	 */
	private SearchIndexBuilderWorker searchIndexBuilderWorker;

	private IndexStorage indexStorage = null;
	/**
	 * the currently running index searcher
	 */
	private IndexSearcher runningIndexSearcher;

	private boolean diagnostics;

	private Object reloadObjectSemaphore = new Object();

	private Timer indexCloseTimer = new Timer(true);

	private long reloadStart;

	private long reloadEnd;

	/**
	 * Register a notification action to listen to events and modify the search
	 * index
	 */
	@Override
	public void init()
	{

		super.init();

		try
		{

			try
			{
				if (autoDdl)
				{
					SqlService.getInstance().ddl(this.getClass().getClassLoader(),
							"sakai_search");
				}
			}
			catch (Exception ex)
			{
				log.error("Perform additional SQL setup", ex);
			}

			initComplete = true;

			if (diagnostics)
			{
				indexStorage.enableDiagnostics();
			}
			else
			{
				indexStorage.disableDiagnostics();
			}

		}
		catch (Throwable t)
		{
			log.error("Failed to start ", t); //$NON-NLS-1$
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void reload()
	{
		getIndexSearcher(true);
	}

	public void forceReload()
	{
		reloadStart = 0;
	}

	/**
	 * The sequence is, peform reload,
	 * 
	 * @param reload
	 * @return
	 */

	@Override
	public IndexSearcher getIndexSearcher(boolean reload)
	{

		if (runningIndexSearcher == null
				|| (reload && !searchIndexBuilderWorker.isLocalLock()))
		{

			// there is a possiblity that we get more than one thread comming
			// through this block.
			// however this could oonly happen if more than one thread went
			// throught the next 3 lines
			// at the same time.
			// if more than one thread did go through the next 3 at the same
			// time, then the local segments
			// might get updated more than once.
			long lastUpdate = indexStorage.getLastUpdate();
			if (lastUpdate > reloadStart || runningIndexSearcher == null)
			{
				reloadStart = System.currentTimeMillis();

				long startLoad = System.currentTimeMillis();
				if (log.isDebugEnabled())
				{
					log.debug("Reloading Index, force=" + reload); //$NON-NLS-1$
				}
				try
				{

					// dont leave closing the index searcher to
					// the
					// GC.
					// It
					// may
					// not happen fast enough.

					// this makes the assumption that getIndexSearcher is thread
					// safe.
					// Care has to be taken here as there may be an update on
					// the index being performed.
					IndexSearcher newRunningIndexSearcher = indexStorage
							.getIndexSearcher();

					synchronized (reloadObjectSemaphore)
					{
						final IndexSearcher oldRunningIndexSearcher = runningIndexSearcher;
						runningIndexSearcher = newRunningIndexSearcher;
						reloadEnd = System.currentTimeMillis();

						if (oldRunningIndexSearcher != null)
						{
							indexCloseTimer.schedule(new TimerTask()
							{

								@Override
								public void run()
								{
									try
									{
										indexStorage
												.closeIndexSearcher(oldRunningIndexSearcher);
									}
									catch (Exception ex)
									{
										log.error("Failed to close old searcher ", ex); //$NON-NLS-1$
									}
								}

							}, 30 * 1000L);
						}
					}

					if (diagnostics)
					{
						log.info("Index Reloaded containing " + getNDocs()
								+ " active documents and  " + getPendingDocs()
								+ " pending documents in " + (reloadEnd - reloadStart)
								+ "ms");
					}
				}
				catch (IOException e)
				{
					reloadStart = reloadEnd;
				}
				long loadPause = System.currentTimeMillis() - startLoad;
				if (loadPause > 10 * 1000L)
				{
					log.warn("Reload of blocked this thread for " + loadPause + " ms ");
				}
			}
			else
			{
				if (log.isDebugEnabled())
				{
					log.debug("No Reload lastUpdate " + lastUpdate //$NON-NLS-1$
							+ " < lastReload " + reloadStart); //$NON-NLS-1$
				}

			}
		}

		return runningIndexSearcher;
	}

	@Override
	public String getStatus()
	{

		String lastLoad = (new Date(reloadEnd)).toString();
		String loadTime = String.valueOf((double) (0.001 * (reloadEnd - reloadStart)));
		SearchWriterLock lock = searchIndexBuilderWorker.getCurrentLock();
		List lockNodes = searchIndexBuilderWorker.getNodeStatus();

		return Messages.getString("SearchServiceImpl.40") + lastLoad + Messages.getString("SearchServiceImpl.38") + loadTime + Messages.getString("SearchServiceImpl.37"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public SearchStatus getSearchStatus()
	{
		String ll = Messages.getString("SearchServiceImpl.36"); //$NON-NLS-1$
		String lt = ""; //$NON-NLS-1$
		if (reloadEnd != 0)
		{
			ll = (new Date(reloadEnd)).toString();
			lt = String.valueOf((double) (0.001 * (reloadEnd - reloadStart)));
		}
		final String lastLoad = ll;
		final String loadTime = lt;
		final SearchWriterLock lock = searchIndexBuilderWorker.getCurrentLock();
		final List lockNodes = searchIndexBuilderWorker.getNodeStatus();
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

			public String getCurrentWorkerETC()
			{
				if (SecurityService.isSuperUser())
				{
					return MessageFormat.format(Messages
							.getString("SearchServiceImpl.35"), //$NON-NLS-1$
							new Object[] { lock.getExpires(),
									searchIndexBuilderWorker.getLastDocument(),
									searchIndexBuilderWorker.getLastElapsed(),
									searchIndexBuilderWorker.getCurrentDocument(),
									searchIndexBuilderWorker.getCurrentElapsed(),
									ServerConfigurationService.getServerIdInstance() });
				}
				else
				{
					return MessageFormat.format(Messages
							.getString("SearchServiceImpl.39"), new Object[] { lock //$NON-NLS-1$
							.getExpires() });
				}
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
						result[2] = Messages.getString("SearchServiceImpl.47"); //$NON-NLS-1$
					}
					else
					{
						result[2] = Messages.getString("SearchServiceImpl.48"); //$NON-NLS-1$
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

	@Override
	public boolean removeWorkerLock()
	{
		return searchIndexBuilderWorker.removeWorkerLock();

	}

	/**
	 * @return Returns the indexStorage.
	 */
	public IndexStorage getIndexStorage()
	{
		return indexStorage;
	}

	/**
	 * @param indexStorage
	 *        The indexStorage to set.
	 */
	public void setIndexStorage(IndexStorage indexStorage)
	{
		this.indexStorage = indexStorage;
	}

	@Override
	public List getSegmentInfo()
	{
		return indexStorage.getSegmentInfoList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#disableDiagnostics()
	 */
	@Override
	public void disableDiagnostics()
	{
		diagnostics = false;
		if (indexStorage != null )
		{
			indexStorage.disableDiagnostics();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#enableDiagnostics()
	 */
	@Override
	public void enableDiagnostics()
	{
		diagnostics = true;
		if (indexStorage != null )
		{
			indexStorage.enableDiagnostics();
		}
	}

	/**
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#hasDiagnostics()
	 */
	@Override
	public boolean hasDiagnostics()
	{
		return diagnostics;
	}

	/**
	 * 
	 * @see org.sakaiproject.search.component.service.impl.BaseSearchServiceImpl#getAnalyzer()
	 */
	@Override
	protected Analyzer getAnalyzer()
	{
		return indexStorage.getAnalyzer();
	}

	/**
	 * @return the searchIndexBuilderWorker
	 */
	public SearchIndexBuilderWorker getSearchIndexBuilderWorker()
	{
		return searchIndexBuilderWorker;
	}

	/**
	 * @param searchIndexBuilderWorker the searchIndexBuilderWorker to set
	 */
	public void setSearchIndexBuilderWorker(SearchIndexBuilderWorker searchIndexBuilderWorker)
	{
		this.searchIndexBuilderWorker = searchIndexBuilderWorker;
	}

}
