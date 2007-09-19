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
import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sakaiproject.search.optimize.api.IndexOptimizeTransaction;
import org.sakaiproject.search.optimize.api.NoOptimizationRequiredException;
import org.sakaiproject.search.optimize.api.OptimizableIndex;
import org.sakaiproject.search.optimize.api.OptimizeTransactionListener;
import org.sakaiproject.search.optimize.api.OptimizedFailedIndexTransactionException;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * An OptimizationTransactionListener that optimizes the index. It first
 * collects the segments that could be optimized. If ther are more than the
 * mergeSize then it will perform the merge into a temporary segment and if that
 * is successfull that index will be merged in the commit phase into the
 * permanent index
 * 
 * @author ieb
 */
public class OptimizeTransactionListenerImpl implements OptimizeTransactionListener
{

	/**
	 * The index to be optimised
	 */
	private OptimizableIndex optimizableIndex;

	/**
	 * The minimum number of segments to perform a merge on
	 */
	private long mergeSize;

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#close(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void close(IndexTransaction transaction) throws IndexTransactionException
	{
		// nothing special to do, the transaction will close itself
	}

	/**
	 * commit closes the temporary segment, and merges it into the permanent
	 * segment. Both the temporary and permanent were opened in the prepare
	 * phase
	 * 
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#commit(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void commit(IndexTransaction transaction) throws IndexTransactionException
	{
		try
		{
			IndexWriter iw = ((IndexOptimizeTransaction) transaction)
					.getTemporaryIndexWriter();
			Directory d = iw.getDirectory();
			iw.close();

			// close the temporary index
			IndexWriter pw = ((IndexOptimizeTransaction) transaction)
					.getPermanentIndexWriter();
			// open the temp writer

			pw.addIndexes(new Directory[] { d });
			pw.optimize();
			pw.close();

			File[] optimzableSegments = ((IndexOptimizeTransaction) transaction)
					.getOptimizableSegments();
			optimizableIndex.removeOptimizableSegments(optimzableSegments);

		}
		catch (IOException ioex)
		{
			throw new OptimizedFailedIndexTransactionException(
					"Failed to commit index merge operation ", ioex);
		}

	}

	/**
	 * Open the index, but throw a NoOptimizationReqiredException if there are
	 * not enough segments to perfom a merge on
	 * 
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#open(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void open(IndexTransaction transaction) throws IndexTransactionException
	{
		File[] optimzableSegments = optimizableIndex.getOptimizableSegments();
		if (optimzableSegments.length < mergeSize)
		{
			throw new NoOptimizationRequiredException();
		}
		((IndexOptimizeTransaction) transaction)
				.setOptimizableSegments(optimzableSegments);

	}

	/**
	 * Perform the merge operation on the segments into a temporary segment,
	 * open the permanent segment to ensure that a writer lock can be aquired
	 * 
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#prepare(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void prepare(IndexTransaction transaction) throws IndexTransactionException
	{
		try
		{
			// open the permanent writer to ensure it can be opened
			IndexWriter pw = optimizableIndex.getPermanentIndexWriter();
			((IndexOptimizeTransaction) transaction).setPermanentIndexWriter(pw);

			// open the temp writer
			IndexWriter iw = ((IndexOptimizeTransaction) transaction)
					.getTemporaryIndexWriter();
			File[] optimzableSegments = ((IndexOptimizeTransaction) transaction)
					.getOptimizableSegments();
			FSDirectory[] directories = new FSDirectory[optimzableSegments.length];
			int i = 0;
			for (File f : optimzableSegments)
			{
				directories[i++] = FSDirectory.getDirectory(f, false);
			}
			iw.addIndexes(directories);
			iw.optimize();

		}
		catch (IOException e)
		{
			throw new OptimizedFailedIndexTransactionException(
					"Failed to Optimize indexes ", e);
		}

	}

	/**
	 * Roll back the optimize operation
	 * 
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#rollback(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void rollback(IndexTransaction transaction) throws IndexTransactionException
	{
		// roll should be handled in the transaction, nothing special to do
		// here.

	}

	/**
	 * @return the mergeSize
	 */
	public long getMergeSize()
	{
		return mergeSize;
	}

	/**
	 * @param mergeSize
	 *        the mergeSize to set
	 */
	public void setMergeSize(long mergeSize)
	{
		this.mergeSize = mergeSize;
	}

	/**
	 * @return the optimizableIndex
	 */
	public OptimizableIndex getOptimizableIndex()
	{
		return optimizableIndex;
	}

	/**
	 * @param optimizableIndex
	 *        the optimizableIndex to set
	 */
	public void setOptimizableIndex(OptimizableIndex optimizableIndex)
	{
		this.optimizableIndex = optimizableIndex;
	}

}
