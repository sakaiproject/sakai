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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.search.journal.api.IndexListener;
import org.sakaiproject.search.util.FileUtils;

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

	private long closeDelay = 30000;

	protected ThreadLocal<Object> inclose = new ThreadLocal<Object>();
	protected ThreadLocal<Object> insearcherclose = new ThreadLocal<Object>();

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
	}

	public void destroy()
	{
		close();
	}

	public void close()
	{
		timer.cancel();
		for (IndexManagementTimerTask itt : tasks)
		{
			itt.setClosed(true);
		}
		closed = true;
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
	public void doIndexReaderClose(final IndexReader oldMultiReader, final File[] toRemove) throws IOException
	{
		if (inclose.get() == null)
		{
			final long closeId = System.currentTimeMillis();
			log.info("Sceduling Close of index with id "+closeId+" and "+toRemove.length+" to remove" );
			timer.schedule(new TimerTask()
			{

				@Override
				public void run()
				{
					inclose.set("closing");
					try
					{
						log.info("Closing "+closeId+"with "+toRemove.length+" files to delete");
						oldMultiReader.close();
						for (File f : toRemove)
						{
							FileUtils.deleteAll(f);
							log.info("Deleted Old Segment "+f);
						}
						log.info("Closed Index");
						
					}
					catch (Exception ex)
					{
						log.warn("Close of old index failed " + ex.getMessage());
					} finally {
						inclose.set(null);
					}
				}

			}, closeDelay);
			throw new IOException("Close Will take place in " + closeDelay + " ms");
		} 

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.IndexListener#doIndexReaderOpen(org.apache.lucene.index.IndexReader)
	 */
	public void doIndexReaderOpen(IndexReader newMultiReader)
	{

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

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.journal.api.IndexListener#doIndexSearcherClose(org.apache.lucene.search.IndexSearcher)
	 */
	public void doIndexSearcherClose(final IndexSearcher indexSearcher) throws IOException
	{
		if (insearcherclose.get() == null)
		{
			timer.schedule(new TimerTask()
			{

				@Override
				public void run()
				{
					insearcherclose.set("closing");
					try
					{
						indexSearcher.close();
						log.info("Closed Index");
						
					}
					catch (Exception ex)
					{
						log.warn("Close of old index failed " + ex.getMessage());
					} finally {
						insearcherclose.set(null);
					}
				}

			}, closeDelay);
			throw new IOException("Close Will take place in " + closeDelay + " ms");
		} 
		
	}

}
