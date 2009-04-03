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

package org.sakaiproject.search.indexer.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.index.impl.ClusterFSIndexStorage;
import org.sakaiproject.search.indexer.api.IndexJournalException;
import org.sakaiproject.search.indexer.api.IndexUpdateTransaction;
import org.sakaiproject.search.indexer.api.IndexUpdateTransactionListener;
import org.sakaiproject.search.journal.api.JournalStorage;
import org.sakaiproject.search.journal.api.JournalStorageState;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * A transaction listener that connects to the journal storage
 * 
 * @author ieb Unit test
 * @see org.sakaiproject.search.indexer.impl.test.TransactionalIndexWorkerTest
 */
public class JournalStorageUpdateTransactionListener implements
		IndexUpdateTransactionListener
{
	private static final Log log = LogFactory.getLog(JournalStorageUpdateTransactionListener.class);
	
	private JournalStorage journalStorage;

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
			String location = ((IndexUpdateTransaction) transaction).getTempIndex();
			long transactionId = transaction.getTransactionId();
			JournalStorageState jss = journalStorage.prepareSave(location, transactionId);
			transaction.put(JournalStorageUpdateTransactionListener.class.getName(), jss);
		}
		catch (Exception ex)
		{
			throw new IndexJournalException("Failed to transfer index ", ex);
		}
		finally
		{
		}

	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#commit(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void commit(IndexTransaction transaction) throws IndexTransactionException
	{
		try
		{
			JournalStorageState jss = (JournalStorageState) transaction
					.get(JournalStorageUpdateTransactionListener.class.getName());
			journalStorage.commitSave(jss);
			transaction.clear(JournalStorageUpdateTransactionListener.class.getName());
		}
		catch (Exception ex)
		{
			throw new IndexJournalException("Failed to commit index ", ex);
		}

	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#open(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void open(IndexTransaction transaction)
	{
	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#rollback(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void rollback(IndexTransaction transaction)
	{
		try
		{
			JournalStorageState jss = (JournalStorageState) transaction
					.get(JournalStorageUpdateTransactionListener.class.getName());
			journalStorage.rollbackSave(jss);
			transaction.clear(JournalStorageUpdateTransactionListener.class.getName());
		}
		catch (Exception ex)
		{
			log.warn("Exception during rollback", ex);
		}
		finally
		{
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#close(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void close(IndexTransaction transaction) throws IndexTransactionException
	{
	}

	/**
	 * @return the journalStorage
	 */
	public JournalStorage getJournalStorage()
	{
		return journalStorage;
	}

	/**
	 * @param journalStorage
	 *        the journalStorage to set
	 */
	public void setJournalStorage(JournalStorage journalStorage)
	{
		this.journalStorage = journalStorage;
	}

}
