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

package org.sakaiproject.search.optimize.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexWriter;
import org.sakaiproject.search.journal.api.JournaledIndex;
import org.sakaiproject.search.optimize.api.OptimizableIndex;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * A class that manages an optimizable index
 * @author ieb
 *
 */
public class OptimizableIndexImpl implements OptimizableIndex
{

	private JournaledIndex journaledIndex;
	
	/**
	 * @see org.sakaiproject.search.optimize.api.OptimizableIndex#getOptimizableSegments()
	 */
	public File[] getOptimizableSegments()
	{
		return journaledIndex.getSegments();
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
	 * @see org.sakaiproject.search.optimize.api.OptimizableIndex#removeOptimizableSegments(java.io.File[])
	 */
	public void removeOptimizableSegments(File[] optimzableSegments)
	{
		List<File> keep = new ArrayList<File>(); 
		List<File> remove = new ArrayList<File>(); 
		for ( File f : getOptimizableSegments() ) {
			keep.add(f);
		}
		
		for ( File f : getOptimizableSegments() ) {
			String optSeg = f.getAbsolutePath();
			for ( File r : optimzableSegments ) {
				if ( optSeg.equals(r.getAbsolutePath()) ) {
					keep.remove(f);
					remove.add(f);
					break;
				}
			}
		}
		journaledIndex.setSegments(keep);
		journaledIndex.removeSegments(remove);
		
		
		
	}

	/**
	 * @return the journaledIndex
	 */
	public JournaledIndex getJournaledIndex()
	{
		return journaledIndex;
	}

	/**
	 * @param journaledIndex the journaledIndex to set
	 */
	public void setJournaledIndex(JournaledIndex journaledIndex)
	{
		this.journaledIndex = journaledIndex;
	}

}
