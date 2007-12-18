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

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.sakaiproject.search.journal.api.IndexCloser;
import org.sakaiproject.search.journal.api.ThreadBinder;
import org.sakaiproject.thread_local.api.ThreadBound;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb
 */
public class RefCountMultiReader extends MultiReader implements ThreadBound, ThreadBinder, IndexCloser
{

	private static final Log log = LogFactory.getLog(RefCountMultiReader.class);

	private IndexReader[] indexReaders;

	private JournaledFSIndexStorage storage;

	private int count = 0;

	private boolean doclose = false;

	private boolean closing = false;

	private ThreadLocalManager threadLocalManager;

	private ConcurrentHashMap<Object, Object> parents = new ConcurrentHashMap<Object, Object>();

	/**
	 * @param arg0
	 * @param storage
	 * @throws IOException
	 */
	public RefCountMultiReader(IndexReader[] indexReaders, JournaledFSIndexStorage storage)
			throws IOException
	{
		super(indexReaders);
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
		if ( threadLocalManager != null )
		{
			threadLocalManager.set(String.valueOf(this), null); // will cause an unbind
		}
		storage.fireIndexReaderClose(this);
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
	
	public boolean forceClose() {
		if ( closing ) return true;
		closing = true;
		if ( log.isDebugEnabled() )
			log.debug("Closing Index "+this);

		try
		{
			super.doClose();
		}
		catch (IOException ex)
		{

		}

		for (IndexReader ir : indexReaders)
		{
			try
			{
				ir.close();
				if ( log.isDebugEnabled() )
					log.debug("Closed " + ir.directory().toString());
			}
			catch (IOException ioex)
			{

			}
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
		if ( threadLocalManager != null ) {
			Object o = threadLocalManager.get(String.valueOf(this));
			if ( o != null ) {
				count--;
				if ( log.isDebugEnabled() )
					log.debug("Unbound "+this+" "+count);
			}
			for ( IndexReader ir : indexReaders ) {
				threadLocalManager.set(String.valueOf(ir), null);
			}
		}

		if (canClose())
		{
			forceClose();
		}
	}

	public void bind(ThreadLocalManager tlm)
	{
		count++;
		threadLocalManager = tlm;
		Object o = tlm.get(String.valueOf(this));
		if ( o == null ) {
			tlm.set(String.valueOf(this),this);
		} else if ( o != this ) {
			log.warn(" More than one object bound to the same key ");
		}
		for ( IndexReader ir : indexReaders ) {
			if ( ir instanceof  ThreadBinder ) {
				((ThreadBinder)ir).bind(tlm);
			}
		}
		if ( log.isDebugEnabled() )
			log.debug("Bind " + this + " "+indexReaders+" " + count);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.impl.IndexCloser#canClose()
	 */
	public boolean canClose()
	{	
		return (count <= 0 && doclose && parents.size() == 0);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.journal.api.IndexCloser#addParent(org.apache.lucene.search.IndexSearcher)
	 */
	public void addParent(Object searcher)
	{
		parents.put(searcher,searcher);
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.journal.api.IndexCloser#removeParent(java.lang.Object)
	 */
	public void removeParent(Object searcher)
	{
		parents.remove(searcher);
		
	}

}
