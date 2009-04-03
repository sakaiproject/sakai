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

import org.sakaiproject.search.indexer.api.IndexJournalException;
import org.sakaiproject.search.indexer.api.IndexUpdateTransactionListener;
import org.sakaiproject.search.journal.api.JournalManager;
import org.sakaiproject.search.journal.api.JournalManagerState;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * A transaction listener that connects to the journalManager
 * 
 * @author ieb Unit test
 * @see org.sakaiproject.search.indexer.impl.test.TransactionalIndexWorkerTest
 */
public class JournalManagerUpdateTransaction implements IndexUpdateTransactionListener
{

	private JournalManager journalManager;

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
		JournalManagerState jms = journalManager.prepareSave(transaction
				.getTransactionId());
		transaction.put(JournalManagerUpdateTransaction.class.getName() + ".state", jms);

	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#commit(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void commit(IndexTransaction transaction) throws IndexTransactionException
	{
		JournalManagerState jms = (JournalManagerState) transaction
				.get(JournalManagerUpdateTransaction.class.getName() + ".state");
		journalManager.commitSave(jms);
		transaction.clear(JournalManagerUpdateTransaction.class.getName() + ".state");

	}

	/**
	 * @throws IndexJournalException
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#open(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void open(IndexTransaction transaction) throws IndexJournalException
	{
		journalManager.doOpenTransaction(transaction);
	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#close(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void close(IndexTransaction transaction) throws IndexTransactionException
	{
		transaction.clear(JournalManagerUpdateTransaction.class.getName() + ".state");
	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#rollback(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void rollback(IndexTransaction transaction)
	{
		JournalManagerState jms = (JournalManagerState) transaction
				.get(JournalManagerUpdateTransaction.class.getName() + ".state");
		journalManager.rollbackSave(jms);
		transaction.clear(JournalManagerUpdateTransaction.class.getName() + ".state");

	}

	/**
	 * @return the journalManager
	 */
	public JournalManager getJournalManager()
	{
		return journalManager;
	}

	/**
	 * @param journalManager
	 *        the journalManager to set
	 */
	public void setJournalManager(JournalManager journalManager)
	{
		this.journalManager = journalManager;
	}

}
