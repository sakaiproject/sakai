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

package org.sakaiproject.search.journal.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.search.journal.api.IndexCloser;
import org.sakaiproject.search.journal.api.ManagementOperation;
import org.sakaiproject.search.journal.api.ThreadBinder;
import org.sakaiproject.thread_local.api.ThreadBound;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb
 */
public class RefCountIndexSearcher extends IndexSearcher implements ThreadBound,
		ThreadBinder, IndexCloser
{

	private static final Log log = LogFactory.getLog(RefCountIndexSearcher.class);

	private int count = 0;

	private boolean doclose = false;

	private JournaledFSIndexStorage storage;

	private IndexReader indexReader;

	private boolean closing = false;

	private ThreadLocalManager threadLocalManager;

	private ThreadLocal<String> unbindingMonitor = new ThreadLocal<String>();
	
	private Object closeMonitor = new Object();

	private ManagementOperation managementOperation; 

	private static int opened = 0;

	/**
	 * @param storage
	 * @param ir
	 */
	public RefCountIndexSearcher(IndexReader indexReader, JournaledFSIndexStorage storage)
	{
		super(indexReader);
		opened++;
		this.managementOperation = ConcurrentIndexManager.getCurrentManagementOperation();
		this.storage = storage;
		this.indexReader = indexReader;
		if (indexReader instanceof IndexCloser)
		{
			((IndexCloser) indexReader).addParent(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.lucene.search.IndexSearcher#close()
	 */
	@Override
	public void close() throws IOException
	{
		doclose = true;
		unbind();
		storage.fireIndexSearcherClose(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.thread_local.api.ThreadBound#unbind()
	 */
	public void unbind()
	{
		Object unbinding = unbindingMonitor.get();
		if (unbinding == null)
		{
			try
			{
				unbindingMonitor.set("unbinding");
				if (threadLocalManager != null)
				{

					Object o = threadLocalManager.get(String.valueOf(this));

					if (o != null)
					{
						count--;
						if (log.isDebugEnabled())
							log.debug("Unbound " + this + " " + count);
						threadLocalManager.set(String.valueOf(this), null); // unbind
						// the
						// dependents
					}
					if (indexReader instanceof ThreadBound)
					{
						((ThreadBound) indexReader).unbind();
					}
				}

				if (canClose())
				{
					forceClose();
				}
			}
			finally
			{
				unbindingMonitor.set(null);
			}
		}
	}

	public void bind(ThreadLocalManager tlm)
	{
		threadLocalManager = tlm;
		Object o = tlm.get(String.valueOf(this));
		if (o == null)
		{
			count++;
			tlm.set(String.valueOf(this), this);
			if (log.isDebugEnabled())
				log.debug("Bind " + this + " " + indexReader + " " + count);
		}
		else if (o != this)
		{
			log.warn(" More than one object bound to the same key ");
		}
		if (indexReader instanceof ThreadBinder)
		{
			((ThreadBinder) indexReader).bind(tlm);
		}

	}

	/**
	 * 
	 */
	public boolean doFinalClose()
	{
		if (canClose())
		{
			return forceClose();
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.impl.IndexCloser#canClose()
	 */
	public boolean canClose()
	{

		return (count <= 0 && doclose);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.impl.IndexCloser#forceClose()
	 */
	public boolean forceClose()
	{
		synchronized (closeMonitor)
		{
			if (closing) return true;
			closing = true;			
		}
		opened --;
		if (indexReader instanceof IndexCloser)
		{
			((IndexCloser) indexReader).removeParent(this);
		}
		if (log.isDebugEnabled()) log.debug("Closing Index " + this);
		try
		{
			super.close();
		}
		catch (IOException ioex)
		{
			log.debug(ioex);

		}
		try
		{
			if (indexReader != null) indexReader.close();
		}
		catch (IOException ioex)
		{

		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.IndexCloser#addParent(org.apache.lucene.search.IndexSearcher)
	 */
	public void addParent(Object searcher)
	{
		// searchers cant have parents
		log.debug("Index Searchers may not have parents, ignored");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.IndexCloser#removeParent(java.lang.Object)
	 */
	public void removeParent(Object searcher)
	{
		log.debug("Index Searchers may not have parents, ignored");
	}

	/**
	 * @return
	 */
	public static int getOpened()
	{
		return opened;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.journal.api.IndexCloser#getName()
	 */
	public String getName()
	{
		return managementOperation+" "+toString()+" Refcount:"+count;
	}

}
