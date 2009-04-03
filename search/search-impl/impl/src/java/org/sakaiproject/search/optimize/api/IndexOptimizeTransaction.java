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

import org.apache.lucene.index.IndexWriter;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * An Index Optimize transaction manages an optimisation operateration where
 * multiple transient segments produced by journaled index operations are merged
 * into a permanent segment
 * 
 * @author ieb
 */
public interface IndexOptimizeTransaction extends IndexTransaction
{

	/**
	 * Get the index writer associated with this transaction
	 * 
	 * @return
	 * @throws IndexTransactionException
	 */
	IndexWriter getTemporaryIndexWriter() throws IndexTransactionException;

	/**
	 * Set the index writer of the permanent index associated with this
	 * transaction
	 * 
	 * @param pw
	 */
	void setPermanentIndexWriter(IndexWriter pw);

	/**
	 * get the permanent index writer associated with this transaction, if it
	 * has been set
	 * 
	 * @return
	 */
	IndexWriter getPermanentIndexWriter();

	/**
	 * Set the list of segments being optimised
	 * 
	 * @param optimzableSegments
	 */
	void setOptimizableSegments(File[] optimzableSegments);

	/**
	 * Get the list of segments being optimised
	 * 
	 * @return
	 */
	File[] getOptimizableSegments();

}
