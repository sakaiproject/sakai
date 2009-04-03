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

package org.sakaiproject.search.optimize.api;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.sakaiproject.search.journal.api.JournalErrorException;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * An optimisable index has a number of segments that could be merged and a
 * permanent index writer into which those segments are merged
 * 
 * @author ieb
 */
public interface OptimizableIndex
{

	/**
	 * Get a list of segments that can be optimized
	 * 
	 * @return
	 * @throws IndexTransactionException
	 */
	File[] getOptimizableSegments() throws IndexTransactionException;

	/**
	 * @return
	 * @throws IndexTransactionException
	 */
	IndexWriter getPermanentIndexWriter() throws IndexTransactionException;

	/**
	 * Remove the supplied list of segments from the optimizable set
	 * 
	 * @param optimzableSegments
	 * @throws IOException
	 * @throws IndexTransactionException
	 */
	void removeOptimizableSegments(File[] optimzableSegments) throws IOException,
			IndexTransactionException;

	/**
	 * Get the number of indexes that could be optimized
	 * 
	 * @return
	 * @throws JournalErrorException
	 */
	int getNumberOfOptimzableSegments() throws IndexTransactionException;

}
