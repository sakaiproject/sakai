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

package org.sakaiproject.search.transaction.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.transaction.api.TransactionIndexManager;
import org.sakaiproject.search.transaction.api.TransactionListener;
import org.sakaiproject.search.transaction.api.TransactionSequence;

/**
 * @author ieb
 */
public abstract class TransactionManagerImpl implements TransactionIndexManager
{

	/**
	 * dependency
	 */
	protected TransactionSequence sequence;

	private List<TransactionListener> transactionListeners = new ArrayList<TransactionListener>();

	public void firePrepare(IndexTransaction transaction)
			throws IndexTransactionException
	{
		for (Iterator<TransactionListener> itl = transactionListeners.iterator(); itl
				.hasNext();)
		{
			TransactionListener tl = itl.next();
			tl.prepare(transaction);
		}
	}

	public void fireCommit(IndexTransaction transaction)
			throws IndexTransactionException
	{
		for (Iterator<TransactionListener> itl = transactionListeners.iterator(); itl
				.hasNext();)
		{
			TransactionListener tl = itl.next();
			tl.commit(transaction);
		}
	}

	/**
	 * @param impl
	 * @throws IndexTransactionException 
	 */
	public void fireClose(IndexTransaction transaction) throws IndexTransactionException
	{
		for (Iterator<TransactionListener> itl = transactionListeners.iterator(); itl
				.hasNext();)
		{
			TransactionListener tl = itl.next();
			tl.close(transaction);
		}
	}

	public void fireRollback(IndexTransaction transaction)
			throws IndexTransactionException
	{
		for (Iterator<TransactionListener> itl = transactionListeners.iterator(); itl
				.hasNext();)
		{
			TransactionListener tl = itl.next();
			tl.rollback(transaction);
		}

	}

	public void fireOpen(IndexTransaction transaction)
			throws IndexTransactionException
	{
		for (Iterator<TransactionListener> itl = transactionListeners.iterator(); itl
				.hasNext();)
		{
			TransactionListener tl = itl.next();
			tl.open(transaction);
		}

	}

	/**
	 * @return the transactionListeners
	 */
	public List<TransactionListener> getTransactionListeners()
	{
		return transactionListeners;
	}

	/**
	 * @param transactionListeners
	 *        the transactionListeners to set
	 */
	public void setTransactionListeners(List<TransactionListener> transactionListeners)
	{
		this.transactionListeners = transactionListeners;
	}

	public void addTransactionListener(TransactionListener transactionListener)
	{
		List<TransactionListener> tl = new ArrayList<TransactionListener>();
		tl.addAll(transactionListeners);
		tl.add(transactionListener);
		transactionListeners = tl;
	}

	public void removeTransactionListener(TransactionListener transactionListener)
	{
		List<TransactionListener> tl = new ArrayList<TransactionListener>();
		tl.addAll(transactionListeners);
		tl.remove(transactionListener);
		transactionListeners = tl;
	}

	/**
	 * @return the sequence
	 */
	public TransactionSequence getSequence()
	{
		return sequence;
	}

	/**
	 * @param sequence
	 *        the sequence to set
	 */
	public void setSequence(TransactionSequence sequence)
	{
		this.sequence = sequence;
	}

}
