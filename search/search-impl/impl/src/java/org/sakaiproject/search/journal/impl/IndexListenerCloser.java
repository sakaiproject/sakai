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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.search.journal.api.IndexCloser;
import org.sakaiproject.search.journal.api.IndexListener;

/**
 * @author ieb
 */
public class IndexListenerCloser implements IndexListener
{

	private static final Log log = LogFactory.getLog(IndexListenerCloser.class);

	private ConcurrentHashMap<IndexCloser, IndexCloser> closeMap = new ConcurrentHashMap<IndexCloser, IndexCloser>();

	public void doIndexReaderClose(IndexReader oldMultiReader) throws IOException
	{
		if (oldMultiReader instanceof IndexCloser)
		{
			closeMap.put((IndexCloser) oldMultiReader, (IndexCloser) oldMultiReader);
		}
		purge();

	}

	public void doIndexReaderOpen(IndexReader newMultiReader)
	{

	}

	public void doIndexSearcherClose(IndexSearcher indexSearcher) throws IOException
	{
		if (indexSearcher instanceof IndexCloser)
		{
			closeMap.put((IndexCloser) indexSearcher, (IndexCloser) indexSearcher);
		}
		purge();
	}

	public void doIndexSearcherOpen(IndexSearcher indexSearcher)
	{

	}

	public void purge()
	{
		for (IndexCloser c : closeMap.values())
		{
			try
			{
				if (c.doFinalClose())
				{
					closeMap.remove(c);
				}
			}
			catch (Exception ex)
			{
				log.info("Auto Index Close Failed " + ex.getMessage());
			}
		}
	}

	/**
	 * @return
	 */
	public int size()
	{
		return closeMap.size();
	}
	
	public String[] getOpenIndexNames() {
		List<String> names = new ArrayList<String>();
		for (IndexCloser ic : closeMap.values() ) {
			names.add(ic.getName());
		}
		return names.toArray(new String[0]);
	}

	/**
	 * 
	 */
	public void cleanup()
	{
		for (IndexCloser ic : closeMap.values())
		{
			ic.forceClose();
		}
		closeMap.clear();
	}

}
