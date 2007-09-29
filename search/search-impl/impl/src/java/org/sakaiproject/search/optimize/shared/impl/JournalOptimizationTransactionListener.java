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

package org.sakaiproject.search.optimize.shared.impl;

import org.sakaiproject.search.indexer.api.IndexJournalException;
import org.sakaiproject.search.indexer.api.IndexUpdateTransactionListener;
import org.sakaiproject.search.journal.api.JournalManager;
import org.sakaiproject.search.journal.api.JournalManagerState;
import org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * A transaction listener that connects to the Journal Optimize Journal manager
 * 
 * @author ieb
 */
public class JournalOptimizationTransactionListener implements
		IndexUpdateTransactionListener
{

	public void init()
	{

	}

	public void destory()
	{

	}

	/**
	 * @throws IndexJournalException
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#prepare(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void prepare(IndexTransaction transaction) throws IndexJournalException
	{
		JournalOptimizationTransaction jtransaction = (JournalOptimizationTransaction) transaction;
		JournalManager journalManager = jtransaction.getJournalManager();
		OptimizeJournalManagerStateImpl jms = (OptimizeJournalManagerStateImpl) journalManager
				.prepareSave(transaction.getTransactionId());
		transaction.put(
				JournalOptimizationTransactionListener.class.getName() + ".state", jms);

		// set the last item to the target and the rest to the merge list
		jtransaction.setTargetSavePoint(jms.mergeList.get(jms.mergeList.size() - 1));
		jms.mergeList.remove(jms.mergeList.size() - 1);
		jtransaction.setMergeList(jms.mergeList);

	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#commit(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void commit(IndexTransaction transaction) throws IndexTransactionException
	{
		JournalOptimizationTransaction jtransaction = (JournalOptimizationTransaction) transaction;
		JournalManager journalManager = jtransaction.getJournalManager();

		JournalManagerState jms = (JournalManagerState) transaction
				.get(JournalOptimizationTransactionListener.class.getName() + ".state");
		journalManager.commitSave(jms);
		transaction.clear(JournalOptimizationTransactionListener.class.getName()
				+ ".state");

	}

	/**
	 * @throws IndexTransactionException
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#open(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void open(IndexTransaction transaction) throws IndexTransactionException
	{
		JournalOptimizationTransaction jtransaction = (JournalOptimizationTransaction) transaction;
		JournalManager journalManager = jtransaction.getJournalManager();

		journalManager.doOpenTransaction(transaction);
	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#close(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void close(IndexTransaction transaction) throws IndexTransactionException
	{
		transaction.clear(JournalOptimizationTransactionListener.class.getName()
				+ ".state");
	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#rollback(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void rollback(IndexTransaction transaction)
	{
		JournalOptimizationTransaction jtransaction = (JournalOptimizationTransaction) transaction;
		JournalManager journalManager = jtransaction.getJournalManager();
		JournalManagerState jms = (JournalManagerState) transaction
				.get(JournalOptimizationTransactionListener.class.getName() + ".state");
		journalManager.rollbackSave(jms);

		transaction.clear(JournalOptimizationTransactionListener.class.getName()
				+ ".state");

	}

}
