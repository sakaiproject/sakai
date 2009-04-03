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

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.indexer.api.IndexUpdateTransaction;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.transaction.api.TransactionListener;

/**
 * Base for index transactions
 * 
 * @author ieb Unit test
 * @see org.sakaiproject.search.indexer.impl.test.TransactionalIndexWorkerTest
 */
public abstract class IndexTransactionImpl implements IndexTransaction
{

	private static final Log log = LogFactory.getLog(IndexTransactionImpl.class);

	protected long transactionId = -2;

	protected TransactionManagerImpl manager;

	protected int transactionState = IndexTransaction.STATUS_UNKNOWN;

	private Map<String, Object> attributes;

	/**
	 * @param m
	 * @param impl
	 * @throws IndexTransactionException
	 */
	public IndexTransactionImpl(TransactionManagerImpl manager, Map<String, Object> m)

	{
		this.manager = manager;
		attributes = m;
	}

	/**
	 * 
	 *
	 */
	public void open() throws IndexTransactionException
	{
		transactionState = IndexTransaction.STATUS_NO_TRANSACTION;
		transactionId = manager.getSequence().getLocalId();
		transactionState = IndexTransaction.STATUS_ACTIVE;
		doBeforeOpen();
		fireOpen(this);
		doAfterOpen();
	}

	/**
	 * 
	 */
	protected void doAfterOpen()
	{

	}

	/**
	 * 
	 */
	protected void doBeforeOpen()
	{

	}

	/**
	 * @throws IndexTransactionException
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#close()
	 */
	public final void close() throws IndexTransactionException
	{
		if (transactionState != IndexTransaction.STATUS_NO_TRANSACTION
				&& transactionState != IndexTransaction.STATUS_COMMITTED
				&& transactionState != IndexTransaction.STATUS_UNKNOWN)
		{
			try
			{
				rollback();
			}
			catch (IndexTransactionException e)
			{
				log.debug(e);
			}
			doBeforeClose();
			fireClose(this);
			doAfterClose();
		}
		transactionState = IndexTransaction.STATUS_UNKNOWN;
	}

	/**
	 * 
	 */
	protected void doAfterClose() throws IndexTransactionException
	{
	}

	/**
	 * 
	 */
	protected void doBeforeClose() throws IndexTransactionException
	{
	}

	/**
	 * @see org.sakaiproject.search.indexer.api.IndexUpdateTransaction#prepare()
	 */
	public final void prepare() throws IndexTransactionException
	{
		if (transactionState != IndexTransaction.STATUS_ACTIVE)
		{
			throw new IndexTransactionException("Transaction is not active ");
		}
		try
		{
			transactionState = IndexTransaction.STATUS_PREPARING;
			doBeforePrepare();
			firePrepare(this);
			doAfterPrepare();
			transactionState = IndexUpdateTransaction.STATUS_PREPARED;
		}
		catch (IndexTransactionException itex)
		{
			throw itex;
		}
		catch (Exception e)
		{
			throw new IndexTransactionException("Failed to prepare ", e);
		}
	}

	/**
	 * 
	 */
	protected void doAfterPrepare() throws IndexTransactionException
	{
	}

	/**
	 * 
	 */
	protected void doBeforePrepare() throws IndexTransactionException
	{
	}

	/**
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#commit()
	 */
	public final void commit() throws IndexTransactionException
	{
		if (transactionState != IndexTransaction.STATUS_PREPARED)
		{
			throw new IndexTransactionException("Transaction is not prepared ");
		}
		try
		{
			transactionState = IndexTransaction.STATUS_COMMITTING;
			doBeforeCommit();
			fireCommit(this);
			doAfterCommit();
			transactionId = -1;
			transactionState = IndexTransaction.STATUS_COMMITTED;
		}
		catch (Exception e)
		{
			throw new IndexTransactionException("Failed to commit ", e);
		}
	}

	/**
	 * 
	 */
	protected void doAfterCommit() throws IndexTransactionException
	{
	}

	/**
	 * 
	 */
	protected void doBeforeCommit() throws IndexTransactionException
	{
	}

	/**
	 * The transaction ID will change as the status cahnges. While the
	 * transaction is active it will have a local ID, when the transaction is
	 * prepared the cluster wide transaction id will be created. Once prepare
	 * has been performed, the transaction should be committed
	 * 
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#getTransactionId()
	 */
	public long getTransactionId()
	{

		return transactionId;
	}

	/**
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#rollback()
	 */
	public final void rollback() throws IndexTransactionException
	{
		if (transactionState != IndexTransaction.STATUS_ACTIVE
				&& transactionState != IndexTransaction.STATUS_COMMITTING
				&& transactionState != IndexTransaction.STATUS_MARKED_ROLLBACK
				&& transactionState != IndexTransaction.STATUS_PREPARED
				&& transactionState != IndexTransaction.STATUS_PREPARING
				&& transactionState != IndexTransaction.STATUS_ROLLING_BACK)
		{
			throw new IndexTransactionException(
					"Transaction cannot be rolled back, state not active, commtting, marked rolled bac, prepared, preparing or rollingback ");
		}
		try
		{
			transactionState = IndexTransaction.STATUS_ROLLING_BACK;
			doBeforeRollback();
			fireRollback(this);
			doAfterRollback();
			transactionId = -1;
			transactionState = IndexTransaction.STATUS_ROLLEDBACK;
		}
		catch (Exception e)
		{

			log.warn("Failed to roll back transaction ", e);
		}
		log.debug("Transaction Rollback Completed on " + this);
	}

	/**
	 * 
	 */
	protected void doAfterRollback() throws IndexTransactionException
	{
	}

	/**
	 * 
	 */
	protected void doBeforeRollback() throws IndexTransactionException
	{
	}

	/**
	 * @see org.sakaiproject.search.indexer.api.IndexUpdateTransaction#getStatus()
	 */
	public final int getStatus()
	{
		return transactionState;
	}

	/**
	 * @see org.sakaiproject.search.indexer.api.IndexUpdateTransaction#clear(java.lang.String)
	 */
	public void clear(String key)
	{
		attributes.remove(key);
	}

	/**
	 * @see org.sakaiproject.search.indexer.api.IndexUpdateTransaction#get(java.lang.String)
	 */
	public Object get(String key)
	{
		return attributes.get(key);
	}

	/**
	 * @see org.sakaiproject.search.indexer.api.IndexUpdateTransaction#put(java.lang.String,
	 *      java.lang.Object)
	 */
	public void put(String key, Object obj)
	{
		attributes.put(key, obj);
	}

	private void firePrepare(IndexTransaction transaction)
			throws IndexTransactionException
	{
		for (Iterator<TransactionListener> itl = manager.getTransactionListeners()
				.iterator(); itl.hasNext();)
		{
			TransactionListener tl = itl.next();
			tl.prepare(transaction);
		}
	}

	private void fireCommit(IndexTransaction transaction)
			throws IndexTransactionException
	{
		for (Iterator<TransactionListener> itl = manager.getTransactionListeners()
				.iterator(); itl.hasNext();)
		{
			TransactionListener tl = itl.next();
			tl.commit(transaction);
		}
	}

	/**
	 * @param impl
	 * @throws IndexTransactionException
	 */
	private void fireClose(IndexTransaction transaction) throws IndexTransactionException
	{
		for (Iterator<TransactionListener> itl = manager.getTransactionListeners()
				.iterator(); itl.hasNext();)
		{
			TransactionListener tl = itl.next();
			tl.close(transaction);
		}
	}

	private void fireRollback(IndexTransaction transaction)
			throws IndexTransactionException
	{
		for (Iterator<TransactionListener> itl = manager.getTransactionListeners()
				.iterator(); itl.hasNext();)
		{
			TransactionListener tl = itl.next();
			tl.rollback(transaction);
		}

	}

	private void fireOpen(IndexTransaction transaction) throws IndexTransactionException
	{
		for (Iterator<TransactionListener> itl = manager.getTransactionListeners()
				.iterator(); itl.hasNext();)
		{
			TransactionListener tl = itl.next();
			tl.open(transaction);
		}

	}

}
