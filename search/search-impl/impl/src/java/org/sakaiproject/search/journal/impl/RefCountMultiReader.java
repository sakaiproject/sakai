/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sakaiproject.search.journal.api.IndexCloser;
import org.sakaiproject.search.journal.api.JournaledIndex;
import org.sakaiproject.search.journal.api.ManagementOperation;
import org.sakaiproject.search.journal.api.ThreadBinder;
import org.sakaiproject.search.util.FileUtils;
import org.sakaiproject.thread_local.api.ThreadBound;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb
 */
public class RefCountMultiReader extends MultiReader implements ThreadBound,
		ThreadBinder, IndexCloser
{

	private static final Log log = LogFactory.getLog(RefCountMultiReader.class);

	private IndexReader[] indexReaders;

	private JournaledFSIndexStorage storage;

	private int count = 0;

	private boolean doclose = false;

	private boolean closing = false;

	private ThreadLocalManager threadLocalManager;

	private ConcurrentHashMap<Object, Object> parents = new ConcurrentHashMap<Object, Object>();

	private ThreadLocal<String> unbindingMonitor = new ThreadLocal<String>();

	private Object closeMonitor = new Object();

	private ManagementOperation managementOperation;

	private static int opened = 0;

	/**
	 * @param arg0
	 * @param storage
	 * @throws IOException
	 */
	public RefCountMultiReader(IndexReader[] indexReaders, JournaledFSIndexStorage storage)
			throws IOException
	{
		super(indexReaders);
		this.managementOperation = ConcurrentIndexManager.getCurrentManagementOperation();
		opened++;
		this.indexReaders = indexReaders;
		this.storage = storage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.lucene.index.MultiReader#doClose()
	 */
	@Override
	protected synchronized void doClose() throws IOException
	{
		doclose = true;
		unbind();
		storage.fireIndexReaderClose(this);
	}

	/**
	 * 
	 */
	public boolean doFinalClose()
	{
		log.debug("doFinalClose() on " + this);
		if (canClose())
		{
			return forceClose();
		}
		return false;
	}

	public boolean forceClose()
	{	
		log.debug("forceClose() on " + this);
		synchronized (closeMonitor)
		{
			if (closing) return true;
			closing = true;
		}

		opened--;
		if (log.isDebugEnabled()) log.debug("Closing Index " + this);

		for (IndexReader ir : indexReaders)
		{
			try
			{
				// close index
				File f = null;
				boolean deleteme = false; 

				try {

					// can we delete any of these indexes ?
					Directory d = ir.directory();
					
					// index closers manage their own delete operations
					if (!(ir instanceof IndexCloser) && (d instanceof FSDirectory))
					{
						FSDirectory fsd = (FSDirectory) d;
						f = fsd.getFile();
						File deleteMarker = new File(f,JournaledIndex.DELETE_ON_CLOSE_FILE);
						deleteme = deleteMarker.exists();
					}
				
					ir.close();

					if (log.isDebugEnabled())
						log.debug("Closed indexreader " + ir);
				}
				catch (AlreadyClosedException acex)
				{
					log.debug("Already closed");
				}

				if (f != null && deleteme)
				{
					FileUtils.deleteAll(f);
					log.debug("Deleting Index on Close " + f);
				}

			}
			catch (IOException ioex)
			{
				log.debug(ioex);
			}
		}

		try
		{
			super.doClose();
		}
		catch (IOException ex)
		{
			log.debug(ex);

		}

		try
		{
			Directory d = this.directory();
			if (d instanceof FSDirectory)
			{
				FSDirectory fsd = (FSDirectory) d;
				File f = fsd.getFile();
				File deleteMarker = new File(f,JournaledIndex.DELETE_ON_CLOSE_FILE);
				if (deleteMarker.exists())
				{
					FileUtils.deleteAll(f);
					log.debug("Deleting Index on Close " + f);
				}
			}
		}
		catch (AlreadyClosedException acex)
		{
			log.debug("Already closed");
		}
		catch (UnsupportedOperationException uoex)
		{
			log.debug(uoex);
		}
		catch (IOException ioex)
		{
			log.debug(ioex);
		}

		return true;

	}

	/**
	 * The isCurrent method in 1.9.1 has a NPE bug, this fixes it
	 * 
	 * @see org.apache.lucene.index.IndexReader#isCurrent()
	 */
	@Override
	public boolean isCurrent() throws IOException
	{
		for (IndexReader ir : indexReaders)
		{
			if (!ir.isCurrent()) return false;
		}
		return true;
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
					for (IndexReader ir : indexReaders)
					{
						if (ir instanceof ThreadBound)
						{
							log.debug("Calling unbind for " + ir);
							((ThreadBound) ir).unbind();
						}

					}
				}

				if (canClose())
				{
					log.debug("Calling forceClose for " + this);
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
				log.debug("Bind " + this + " " + indexReaders + " " + count);
		}
		else if (o != this)
		{
			log.warn(" More than one object bound to the same key ");
		}
		for (IndexReader ir : indexReaders)
		{
			if (ir instanceof ThreadBinder)
			{
				((ThreadBinder) ir).bind(tlm);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.impl.IndexCloser#canClose()
	 */
	public boolean canClose()
	{
		synchronized (parents) {
			return (count <= 0 && doclose && parents.size() == 0);
			
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.IndexCloser#addParent(org.apache.lucene.search.IndexSearcher)
	 */
	public void addParent(Object searcher)
	{
		synchronized (parents) {
			parents.put(searcher, searcher);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.IndexCloser#removeParent(java.lang.Object)
	 */
	public void removeParent(Object searcher)
	{
		synchronized (parents) {
			parents.remove(searcher);			
		}

	}

	/**
	 * @return
	 */
	public static int getOpened()
	{
		return opened;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.IndexCloser#getName()
	 */
	public String getName()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(managementOperation).append(" ");
		sb.append(toString()).append(" RefCount:").append(count);
		for (IndexReader ir : indexReaders)
		{
			sb.append("[").append(ir.directory().toString()).append("]");
		}
		return sb.toString();
	}

}
