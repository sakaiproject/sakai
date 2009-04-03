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

import org.sakaiproject.search.indexer.api.IndexJournalException;
import org.sakaiproject.search.indexer.api.IndexUpdateTransactionListener;
import org.sakaiproject.search.journal.api.JournalStorageState;
import org.sakaiproject.search.journal.impl.SharedFilesystemJournalStorage;
import org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * A transaction listener that connects to the journalManager and saves segments
 * 
 * @author ieb
 */
public class SharedFilesystemSaveTransactionListener implements
		IndexUpdateTransactionListener
{

	private SharedFilesystemJournalStorage sharedFilesystemJournalStorage;

	private long sharedSleep = 90000;

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
			String indexSpace = jtransaction.getWorkingSegment().getAbsolutePath();

			long targetSavePoint = jtransaction.getTargetSavePoint();

			JournalStorageState jss = sharedFilesystemJournalStorage.prepareSave(
					indexSpace, targetSavePoint);
			transaction.put(SharedFilesystemSaveTransactionListener.class.getName()
					+ ".state", jss);
		}
		catch (Exception ex)
		{
			throw new IndexJournalException(
					"Failed to save optimized Journaled Segments ", ex);
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
			JournalStorageState jss = (JournalStorageState) transaction
					.get(SharedFilesystemSaveTransactionListener.class.getName()
							+ ".state");
			sharedFilesystemJournalStorage.commitSave(jss);
			try
			{
				System.err.println("============Sleeping for 90");
				// sleep for 90s to make NFS happy
				Thread.sleep(sharedSleep);
			}
			catch (Exception ex)
			{
				int x =1; // ignore

			}

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
		transaction.clear(SharedFilesystemSaveTransactionListener.class.getName()
				+ ".state");
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
			JournalStorageState jss = (JournalStorageState) transaction
					.get(SharedFilesystemSaveTransactionListener.class.getName()
							+ ".state");
			sharedFilesystemJournalStorage.rollbackSave(jss);
			try
			{
				// sleep for 90s to make NFS happy
				Thread.sleep(sharedSleep);
			}
			catch (Exception ex)
			{
				int x = 1; //ignore 

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

	/**
	 * @return the sharedSleep
	 */
	public long getSharedSleep()
	{
		return sharedSleep;
	}

	/**
	 * @param sharedSleep the sharedSleep to set
	 */
	public void setSharedSleep(long sharedSleep)
	{
		this.sharedSleep = sharedSleep;
	}

}
