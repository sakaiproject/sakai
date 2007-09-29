/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.search.journal.impl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.search.journal.api.IndexListener;

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

	private long closeDelay = 10000;

	protected ThreadLocal<Object> inclose = new ThreadLocal<Object>();

	private DelayQueue<Delayed> delayQueue = new DelayQueue<Delayed>();

	private int nsopen = 0;

	private int nropen = 0;

	public void destory()
	{

	}

	public void init()
	{
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
		timer.schedule(new TimerTask()
		{

			@Override
			public void run()
			{
				try
				{

					inclose.set("xxx");
					log
							.info("Start Purge ------------------------- "
									+ delayQueue.size());
					DelayedClose dc = (DelayedClose) delayQueue.poll();
					while (dc != null)
					{
						dc.close();
						dc = (DelayedClose) delayQueue.poll();
					}
					log.info("Purge complete ----------------------" + delayQueue.size());
				}
				finally
				{
					inclose.set(null);
				}
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
		inclose.set("inclose");
		while (delayQueue.size() > 0)
		{

			DelayedClose dc = (DelayedClose) delayQueue.poll();
			if (dc != null)
			{
				dc.close();
			}
		}
		inclose.set(null);
		closed = true;

		log.info("N Searchers is " + nsopen);
		log.info("N Readers is " + nropen);
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
	public void doIndexReaderClose(IndexReader oldMultiReader, File[] toRemove)
			throws IOException
	{
		if (inclose.get() == null)
		{
			nropen--;
			log.debug("Closed Readerr " + nropen + " " + oldMultiReader);
			delayQueue.offer(new DelayedIndexReaderClose(closeDelay, oldMultiReader,
					toRemove));
			throw new IOException("Close Will take place in " + closeDelay + " ms");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.IndexListener#doIndexSearcherClose(org.apache.lucene.search.IndexSearcher)
	 */
	public void doIndexSearcherClose(IndexSearcher indexSearcher) throws IOException
	{
		if (inclose.get() == null)
		{
			nsopen--;
			log.debug("Closed Searcher " + nsopen + " " + indexSearcher);
			delayQueue.offer(new DelayedIndexSearcherClose(closeDelay, indexSearcher));
			throw new IOException("Close Will take place in " + closeDelay + " ms");
		}

	}

	/**
	 * @see org.sakaiproject.search.journal.api.IndexListener#doIndexSearcherOpen(org.apache.lucene.search.IndexSearcher)
	 */
	public void doIndexSearcherOpen(IndexSearcher indexSearcher)
	{
		nsopen++;
		log.debug(this + "Opened New Searcher " + nsopen + " " + indexSearcher);
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

	}

	/**
	 * @return the closeDelay
	 */
	public long getCloseDelay()
	{
		return closeDelay;
	}

	/**
	 * @param closeDelay
	 *        the closeDelay to set
	 */
	public void setCloseDelay(long closeDelay)
	{
		this.closeDelay = closeDelay;
	}

}
