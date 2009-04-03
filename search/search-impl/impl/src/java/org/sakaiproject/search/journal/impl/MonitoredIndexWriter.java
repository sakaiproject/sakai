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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.sakaiproject.search.journal.api.IndexMonitorListener;

/**
 * An index writer where you can monitor the close operations
 * 
 * @author ieb
 */
public class MonitoredIndexWriter extends IndexWriter
{

	private List<IndexMonitorListener> indexMonitorListeners = new ArrayList<IndexMonitorListener>();

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @throws IOException
	 */
	public MonitoredIndexWriter(String arg0, Analyzer arg1, boolean arg2)
			throws IOException
	{
		super(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @throws IOException
	 */
	public MonitoredIndexWriter(File arg0, Analyzer arg1, boolean arg2)
			throws IOException
	{
		super(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @throws IOException
	 */
	public MonitoredIndexWriter(Directory arg0, Analyzer arg1, boolean arg2)
			throws IOException
	{
		super(arg0, arg1, arg2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.lucene.index.IndexWriter#close()
	 */
	@Override
	public synchronized void close() throws IOException
	{
		super.close();
		fireIndexClosed();
	}

	/**
	 * 
	 */
	private void fireIndexClosed()
	{
		for (Iterator<IndexMonitorListener> itl = indexMonitorListeners.iterator(); itl
				.hasNext();)
		{
			IndexMonitorListener tl = itl.next();
			tl.doIndexMonitorClose(this);
		}
	}

	/**
	 * @return
	 */
	private List<IndexMonitorListener> getMonitorIndexListeners()
	{
		return indexMonitorListeners;
	}

	public void addMonitorIndexListener(IndexMonitorListener indexListener)
	{
		List<IndexMonitorListener> tl = new ArrayList<IndexMonitorListener>();
		tl.addAll(this.indexMonitorListeners);
		tl.add(indexListener);
		this.indexMonitorListeners = tl;
	}

	public void setMonitorIndexListener(List<IndexMonitorListener> indexListeners)
	{
		List<IndexMonitorListener> tl = new ArrayList<IndexMonitorListener>();
		tl.addAll(indexListeners);
		this.indexMonitorListeners = tl;
	}

}
