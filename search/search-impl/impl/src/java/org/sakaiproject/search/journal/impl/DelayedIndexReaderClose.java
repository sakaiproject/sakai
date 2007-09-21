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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.sakaiproject.search.util.FileUtils;

/**
 * @author ieb
 */
public class DelayedIndexReaderClose extends DelayedClose
{

	private static final Log log = LogFactory.getLog(DelayedIndexReaderClose.class);

	private IndexReader reader;

	private File[] toRemove;

	/**
	 * @param oldMultiReader
	 * @param toRemove
	 * @param inclose
	 */
	public DelayedIndexReaderClose(long delay, IndexReader oldMultiReader, File[] toRemove)
	{
		super(delay);
		this.reader = oldMultiReader;
		this.toRemove = toRemove;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.impl.DelayedClose#close()
	 */
	@Override
	protected void close()
	{
		try
		{
			try
			{
				reader.close();
			}
			catch (Exception ex)
			{

			}
			for (File f : toRemove)
			{
				FileUtils.deleteAll(f);
			}

		}
		catch (Exception ex)
		{
			log.warn("Close of old index failed " + ex.getMessage());
		}
		finally
		{
			reader = null;
		}
	}

}
