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

package org.sakaiproject.search.optimize.shared.impl;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.indexer.api.IndexJournalException;
import org.sakaiproject.search.indexer.api.IndexUpdateTransactionListener;
import org.sakaiproject.search.journal.impl.SharedFilesystemJournalStorage;
import org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.util.FileUtils;

/**
 * A transaction listener that connects to the journalManager and loads segments
 * to be optimized
 * 
 * @author ieb
 */
public class SharedFilesystemLoadTransactionListener implements
		IndexUpdateTransactionListener
{

	private static final Log log = LogFactory
			.getLog(SharedFilesystemLoadTransactionListener.class);

	private SharedFilesystemJournalStorage sharedFilesystemJournalStorage;

	public void init()
	{

	}

	public void destroy()
	{

	}

	/**
	 * @throws IndexJournalException
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#prepare(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void prepare(IndexTransaction transaction) throws IndexJournalException
	{
		try
		{
			JournalOptimizationTransaction jtransaction = (JournalOptimizationTransaction) transaction;
			String workingSpace = jtransaction.getWorkingSpace();

			for (long savePoint : jtransaction.getMergeList())
			{
				sharedFilesystemJournalStorage.retrieveSavePoint(savePoint, workingSpace);
				jtransaction.addMergeSegment(sharedFilesystemJournalStorage
						.getLocalJournalLocation(savePoint, workingSpace));
			}
			sharedFilesystemJournalStorage.retrieveSavePoint(jtransaction
					.getTargetSavePoint(), workingSpace);
			
			jtransaction.setTargetSegment(sharedFilesystemJournalStorage
					.getLocalJournalLocation(jtransaction.getTargetSavePoint(),
							workingSpace));
		}
		catch (Exception ex)
		{
			throw new IndexJournalException(
					"Failed to retrieve Journaled Segments for processing ", ex);
		}

	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#commit(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void commit(IndexTransaction transaction) throws IndexTransactionException
	{
		try
		{
			JournalOptimizationTransaction jtransaction = (JournalOptimizationTransaction) transaction;
			List<Long> mergeList = jtransaction.getMergeList();
			// dont delete the last shared segment, we want to keep all versions
			mergeList.remove(mergeList.size()-1);
			for (long savePoint : jtransaction.getMergeList())
			{
				sharedFilesystemJournalStorage.removeJournal(savePoint);
			}
			// we will leave previous versions of this save point in place in the shared
			// space, only allowing them to be cleaned up as a whole.
			// there will be no trimming of versions of a segment
			for (File f : jtransaction.getMergeSegmentList())
			{
				log.debug("Deleting Segment " + f.getPath());
				FileUtils.deleteAll(f);
			}
			log.debug("Deleting Segment " + jtransaction.getTargetSegment().getPath());
			FileUtils.deleteAll(jtransaction.getTargetSegment());

		}
		catch (Exception ex)
		{
			throw new IndexJournalException(
					"Failed to retrieve Journaled Segments for processing ", ex);
		}

	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#open(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void open(IndexTransaction transaction)
	{
	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#close(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void close(IndexTransaction transaction) throws IndexTransactionException
	{
	}

	/**
	 * @throws IndexJournalException
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#rollback(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void rollback(IndexTransaction transaction) throws IndexJournalException
	{
		try
		{
			JournalOptimizationTransaction jtransaction = (JournalOptimizationTransaction) transaction;
			if (jtransaction.getMergeSegmentList() != null)
			{
				for (File f : jtransaction.getMergeSegmentList())
				{
					FileUtils.deleteAll(f);
				}
			}
			if (jtransaction.getTargetSegment() != null)
			{
				FileUtils.deleteAll(jtransaction.getTargetSegment());
			}

		}
		catch (Exception ex)
		{
			throw new IndexJournalException("Failed to rollback Journaled Segments ", ex);
		}

	}

	/**
	 * @return the sharedFilesystemJournalStorage
	 */
	public SharedFilesystemJournalStorage getSharedFilesystemJournalStorage()
	{
		return sharedFilesystemJournalStorage;
	}

	/**
	 * @param sharedFilesystemJournalStorage
	 *        the sharedFilesystemJournalStorage to set
	 */
	public void setSharedFilesystemJournalStorage(
			SharedFilesystemJournalStorage sharedFilesystemJournalStorage)
	{
		this.sharedFilesystemJournalStorage = sharedFilesystemJournalStorage;
	}

}
