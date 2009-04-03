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

package org.sakaiproject.search.index.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.search.index.AnalyzerFactory;
import org.sakaiproject.search.index.IndexReloadListener;
import org.sakaiproject.search.index.IndexStorage;

/**
 * @author ieb
 */
public abstract class BaseIndexStorage implements IndexStorage
{

	private static final Log log = LogFactory.getLog(BaseIndexStorage.class);

	/**
	 * the currently running index searcher
	 */
	private IndexSearcher runningIndexSearcher;

	private Object reloadObjectSemaphore = new Object();

	private Timer indexCloseTimer = new Timer(true);

	private long reloadStart;

	private long reloadEnd;

	protected boolean diagnostics;

	private List<IndexReloadListener> indexReloadListeners = new ArrayList<IndexReloadListener>();

	/**
	 * The token analyzer
	 */
	private AnalyzerFactory analyzerFactory = null;

	public IndexSearcher getIndexSearcher(boolean reload) throws IOException
	{

		if (runningIndexSearcher == null || (reload))
		{

			// there is a possiblity that we get more than one thread comming
			// through this block.
			// however this could oonly happen if more than one thread went
			// throught the next 3 lines
			// at the same time.
			// if more than one thread did go through the next 3 at the same
			// time, then the local segments
			// might get updated more than once.
			long lastUpdate = getLastUpdate();
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
					IndexSearcher newRunningIndexSearcher = getIndexSearcher();

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
										closeIndexSearcher(oldRunningIndexSearcher);
									}
									catch (Exception ex)
									{
										log.error("Failed to close old searcher ", ex); //$NON-NLS-1$
									}
									finally
									{
										cancel();
										indexCloseTimer.purge();
									}
								}

							}, 30 * 1000L);
						}
					}

					fireIndexReload(reloadStart, reloadEnd);
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

	/**
	 * @return
	 */
	protected abstract IndexSearcher getIndexSearcher() throws IOException;

	/**
	 * @param reloadStart2
	 * @param reloadEnd2
	 */
	protected void fireIndexReload(long reloadStart, long reloadEnd)
	{
		for (Iterator<IndexReloadListener> itl = indexReloadListeners.iterator(); itl
				.hasNext();)
		{
			IndexReloadListener tl = itl.next();
			tl.reloaded(reloadStart, reloadEnd);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getLastLoad()
	 */
	public long getLastLoad()
	{
		return reloadEnd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getLastLoadTime()
	 */
	public long getLastLoadTime()
	{
		return (reloadEnd - reloadStart);
	}

	/**
	 * @see org.sakaiproject.search.index.IndexStorage#addReloadListener(IndexReloadListener
	 *      indexReloadListener)
	 */

	public void addReloadListener(IndexReloadListener indexReloadListener)
	{
		List<IndexReloadListener> tl = new ArrayList<IndexReloadListener>();
		tl.addAll(indexReloadListeners);
		tl.add(indexReloadListener);
		indexReloadListeners = tl;
	}

	/**
	 * @see org.sakaiproject.search.index.IndexStorage#removeReloadListener(IndexReloadListener
	 *      indexReloadListener)
	 */
	public void removeReloadListener(IndexReloadListener indexReloadListener)
	{
		List<IndexReloadListener> tl = new ArrayList<IndexReloadListener>();
		tl.addAll(indexReloadListeners);
		tl.remove(indexReloadListener);
		indexReloadListeners = tl;
	}

	/**
	 * @see org.sakaiproject.search.index.IndexStorage#forceNextReload()
	 */
	public void forceNextReload()
	{
		reloadStart = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#disableDiagnostics()
	 */
	public void disableDiagnostics()
	{
		diagnostics = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#enableDiagnostics()
	 */
	public void enableDiagnostics()
	{
		diagnostics = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#hasDiagnostics()
	 */
	public boolean hasDiagnostics()
	{
		return diagnostics;
	}

	/**
	 * @return Returns the analzyserFactory.
	 */
	public AnalyzerFactory getAnalyzerFactory()
	{
		return analyzerFactory;
	}

	/**
	 * @param analzyserFactory
	 *        The analzyserFactory to set.
	 */
	public void setAnalyzerFactory(AnalyzerFactory analzyserFactory)
	{
		this.analyzerFactory = analzyserFactory;
	}

	public Analyzer getAnalyzer()
	{
		return analyzerFactory.newAnalyzer();
	}

}
