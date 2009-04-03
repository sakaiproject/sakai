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

package org.sakaiproject.search.journal.impl;

import java.util.List;
import java.util.Map;

import org.sakaiproject.search.journal.api.MergeTransactionListener;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.transaction.api.TransactionListener;
import org.sakaiproject.search.transaction.impl.TransactionManagerImpl;

/**
 * Manages the index update operations
 * 
 * @author ieb TODO Unit test
 */
public class MergeUpdateManager extends TransactionManagerImpl
{

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionIndexManager#openTransaction(java.util.Map)
	 */
	public IndexTransaction openTransaction(Map<String, Object> m)
			throws IndexTransactionException
	{
		IndexTransaction it = new IndexMergeTransactionImpl(this, m);
		it.open();
		return it;
	}

	public void init()
	{

	}

	public void destroy()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.transaction.impl.TransactionManagerImpl#addTransactionListener(org.sakaiproject.search.transaction.api.TransactionListener)
	 */
	@Override
	public void addTransactionListener(TransactionListener transactionListener)
	{
		if (transactionListener instanceof MergeTransactionListener)
		{
			super.addTransactionListener(transactionListener);
		}
		else
		{
			throw new RuntimeException(
					"transactionListener must implement MergeTransactionListener "
							+ transactionListener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.transaction.impl.TransactionManagerImpl#setTransactionListeners(java.util.List)
	 */
	@Override
	public void setTransactionListeners(List<TransactionListener> transactionListeners)
	{
		for (TransactionListener tl : transactionListeners)
		{
			if (!(tl instanceof MergeTransactionListener))
			{
				throw new RuntimeException(
						"transactionListener must implement MergeTransactionListener "
								+ tl);
			}
		}
		super.setTransactionListeners(transactionListeners);
	}

}
