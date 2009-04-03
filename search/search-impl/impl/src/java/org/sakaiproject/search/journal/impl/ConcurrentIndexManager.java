/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.search.journal.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.journal.api.IndexListener;
import org.sakaiproject.search.journal.api.ManagementOperation;

/**
 * The ConcurrentIndexManager, manages a single thread performs a number of
 * tasks associated with index management.
 * 
 * @author ieb Unit test
 * @see org.sakaiproject.search.indexer.impl.test.ConcurrentIndexManagerTest
 */
public class ConcurrentIndexManager implements IndexListener
{
	protected static final Log log = LogFactory.getLog(ConcurrentIndexManager.class);

	private Timer timer = new Timer(true);;

	private List<IndexManagementTimerTask> tasks;

	private boolean closed = false;

	private int nsopen = 0;

	private int nropen = 0;

	private SearchService searchService;

	private IndexListenerCloser indexListenerCloser;

	private static ThreadLocal<ManagementOperation> runningOperation = new ThreadLocal<ManagementOperation>();

	public void init()
	{

		if (!searchService.isEnabled())
		{
			return;
		}

		for (Iterator<IndexManagementTimerTask> i = tasks.iterator(); i.hasNext();)
		{
			IndexManagementTimerTask task = i.next();
			if (task.isFixedRate())
			{
				timer.scheduleAtFixedRate(task, task.getDelay(), task.getPeriod());
			}
			else
			{
				timer.schedule(task, task.getDelay(), task.getPeriod());

			}
		}
		if ( indexListenerCloser == null ) {
			indexListenerCloser = new IndexListenerCloser();
		}
		timer.schedule(new TimerTask()
		{

			@Override
			public void run()
			{
				log.debug("Start Purge ------------------------- " + indexListenerCloser.size());
				indexListenerCloser.purge();
				log.debug("Purge complete ----------------------" + indexListenerCloser.size());
			}

		}, 5000, 15000);
	}

	public void destroy()
	{
		close();
		cleanup();
	}

	public void close()
	{
		timer.cancel();
		for (IndexManagementTimerTask itt : tasks)
		{
			itt.setClosed(true);
		}
	}

	public void cleanup()
	{
		indexListenerCloser.cleanup();
		closed = true;

		log.debug("N Searchers is " + nsopen);
		log.debug("N Readers is " + nropen);
	}

	/**
	 * @return the tasks
	 */
	public List<IndexManagementTimerTask> getTasks()
	{
		return tasks;
	}

	/**
	 * @param tasks
	 *        the tasks to set
	 */
	public void setTasks(List<IndexManagementTimerTask> tasks)
	{
		this.tasks = tasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.IndexListener#doIndexReaderClose(org.apache.lucene.index.IndexReader)
	 */
	public void doIndexReaderClose(IndexReader oldMultiReader) throws IOException
	{
		nropen--;
		log.debug("Closed Reader " + nropen + " " + oldMultiReader);
		indexListenerCloser.doIndexReaderClose(oldMultiReader);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.IndexListener#doIndexSearcherClose(org.apache.lucene.search.IndexSearcher)
	 */
	public void doIndexSearcherClose(IndexSearcher indexSearcher) throws IOException
	{
		nsopen--;
		log.debug("Closed Searcher " + nsopen + " " + indexSearcher);
		indexListenerCloser.doIndexSearcherClose(indexSearcher);

	}

	/**
	 * @see org.sakaiproject.search.journal.api.IndexListener#doIndexSearcherOpen(org.apache.lucene.search.IndexSearcher)
	 */
	public void doIndexSearcherOpen(IndexSearcher indexSearcher)
	{
		nsopen++;
		log.debug(this + "Opened New Searcher " + nsopen + " " + indexSearcher);
		indexListenerCloser.doIndexSearcherOpen(indexSearcher);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.IndexListener#doIndexReaderOpen(org.apache.lucene.index.IndexReader)
	 */
	public void doIndexReaderOpen(IndexReader newMultiReader)
	{
		nropen++;
		log.debug("Opened New Reader " + nropen + " " + newMultiReader);
		indexListenerCloser.doIndexReaderOpen(newMultiReader);

	}


	/**
	 * @return the searchService
	 */
	public SearchService getSearchService()
	{
		return searchService;
	}

	/**
	 * @param searchService
	 *        the searchService to set
	 */
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	/**
	 * @return the indexListenerCloser
	 */
	public IndexListenerCloser getIndexListenerCloser()
	{
		return indexListenerCloser;
	}

	/**
	 * @param indexListenerCloser the indexListenerCloser to set
	 */
	public void setIndexListenerCloser(IndexListenerCloser indexListenerCloser)
	{
		this.indexListenerCloser = indexListenerCloser;
	}

	/**
	 * @param managementOperation
	 */
	public static void setRunning(ManagementOperation managementOperation)
	{
		runningOperation.set(managementOperation);	
	}

	/**
	 * @return
	 */
	public static ManagementOperation getCurrentManagementOperation()
	{
		return runningOperation.get();
	}
	
	

}
