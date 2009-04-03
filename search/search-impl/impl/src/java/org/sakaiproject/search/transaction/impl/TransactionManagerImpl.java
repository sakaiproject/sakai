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

package org.sakaiproject.search.transaction.impl;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.search.transaction.api.TransactionIndexManager;
import org.sakaiproject.search.transaction.api.TransactionListener;
import org.sakaiproject.search.transaction.api.TransactionSequence;

/**
 * @author ieb Unit test
 * @see org.sakaiproject.search.indexer.impl.test.TransactionalIndexWorkerTest
 */
public abstract class TransactionManagerImpl implements TransactionIndexManager
{

	private List<TransactionListener> transactionListeners = new ArrayList<TransactionListener>();

	/**
	 * dependency
	 */
	protected TransactionSequence sequence;

	public void init()
	{

	}

	public void destroy()
	{

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
