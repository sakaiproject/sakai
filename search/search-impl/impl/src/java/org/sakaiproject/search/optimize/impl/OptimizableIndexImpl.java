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

package org.sakaiproject.search.optimize.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.sakaiproject.search.journal.api.JournaledIndex;
import org.sakaiproject.search.optimize.api.OptimizableIndex;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * A class that manages an optimizable index
 * 
 * @author ieb
 */
public class OptimizableIndexImpl implements OptimizableIndex
{

	private static final Log log = LogFactory.getLog(OptimizableIndexImpl.class);

	private JournaledIndex journaledIndex;

	public void init()
	{

	}

	public void destroy()
	{

	}

	/**
	 * @see org.sakaiproject.search.optimize.api.OptimizableIndex#getOptimizableSegments()
	 */
	public File[] getOptimizableSegments()
	{
		return journaledIndex.getSegments();
	}

	/**
	 * @see org.sakaiproject.search.optimize.api.OptimizableIndex#getNumberOfOptimzableSegments()
	 */
	public int getNumberOfOptimzableSegments()
	{
		return journaledIndex.getSegments().length;
	}

	/**
	 * @throws IndexTransactionException
	 * @see org.sakaiproject.search.optimize.api.OptimizableIndex#getPermanentIndexWriter()
	 */
	public IndexWriter getPermanentIndexWriter() throws IndexTransactionException
	{
		return journaledIndex.getPermanentIndexWriter();
	}

	/**
	 * @throws IOException
	 * @see org.sakaiproject.search.optimize.api.OptimizableIndex#removeOptimizableSegments(java.io.File[])
	 */
	public void removeOptimizableSegments(File[] optimzableSegments) throws IOException
	{
		List<File> keep = new ArrayList<File>();
		List<File> remove = new ArrayList<File>();
		for (File f : getOptimizableSegments())
		{
			keep.add(f);
		}

		for (File f : getOptimizableSegments())
		{
			String optSeg = f.getAbsolutePath();
			for (File r : optimzableSegments)
			{
				if (optSeg.equals(r.getAbsolutePath()))
				{
					keep.remove(f);
					remove.add(f);
					break;
				}
			}
		}
		log.info("Keeping " + keep.size() + " removing " + remove.size() + " segments");
		journaledIndex.setSegments(keep);
		journaledIndex.saveSegmentList();

	}

	/**
	 * @return the journaledIndex
	 */
	public JournaledIndex getJournaledIndex()
	{
		return journaledIndex;
	}

	/**
	 * @param journaledIndex
	 *        the journaledIndex to set
	 */
	public void setJournaledIndex(JournaledIndex journaledIndex)
	{
		this.journaledIndex = journaledIndex;
	}

}
