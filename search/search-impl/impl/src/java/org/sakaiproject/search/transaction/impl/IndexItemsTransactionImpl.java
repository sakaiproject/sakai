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

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.transaction.api.IndexItemsTransaction;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * Base for index transactions with items
 * 
 * @author ieb
 * @see org.sakaiproject.search.indexer.impl.test.TransactionalIndexWorkerTest
 */
public abstract class IndexItemsTransactionImpl extends IndexTransactionImpl implements
		IndexItemsTransaction
{

	private static final Log log = LogFactory.getLog(IndexItemsTransactionImpl.class);

	private List<SearchBuilderItem> itemList;

	/**
	 * @param m
	 * @param impl
	 * @throws IndexTransactionException
	 */
	public IndexItemsTransactionImpl(TransactionManagerImpl manager, Map<String, Object> m)
	{
		super(manager, m);
	}

	public void setItems(List<SearchBuilderItem> items) throws IndexTransactionException
	{
		if (transactionState != IndexTransaction.STATUS_ACTIVE)
		{
			throw new IndexTransactionException("Transaction is not active ");
		}
		if (itemList != null)
		{
			throw new IndexTransactionException(
					"Once the items has been set, it cannot be reset while the transaction is in process");
		}

		itemList = items;
	}

	/**
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#getItems()
	 */
	public List<SearchBuilderItem> getItems()
	{
		return itemList;
	}

}
