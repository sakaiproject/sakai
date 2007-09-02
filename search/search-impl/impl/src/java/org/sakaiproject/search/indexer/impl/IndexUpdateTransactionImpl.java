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

package org.sakaiproject.search.indexer.impl;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.sakaiproject.search.indexer.api.IndexTransactionException;
import org.sakaiproject.search.indexer.api.IndexUpdateTransaction;
import org.sakaiproject.search.indexer.api.NoItemsToIndexException;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.util.FileUtils;

/**
 * @author ieb
 *
 */
public class IndexUpdateTransactionImpl implements IndexUpdateTransaction
{

	private static final Log log = LogFactory.getLog(IndexUpdateTransactionImpl.class);

	protected static final String TRANSACTION_LIST = "sakai_tx";

	private long transactionId = -2;

	private IndexWriter indexWriter = null;

	private File tempIndex = null;

	private List<SearchBuilderItem> txList = null;

	private TransactionIndexManagerImpl manager = null;

	private List<SearchBuilderItem> itemList = null;
	
	private int transactionState = IndexUpdateTransaction.STATUS_UNKNOWN;

	private Map<String, Object> attributes;


	/**
	 * @param m 
	 * @param impl
	 * @throws IndexTransactionException 
	 */
	public IndexUpdateTransactionImpl(TransactionIndexManagerImpl manager, Map<String, Object> m) throws IndexTransactionException
	{
		transactionState = IndexUpdateTransaction.STATUS_NO_TRANSACTION;
		this.manager  = manager;
		transactionId = manager.sequence.getLocalId();	
		transactionState = IndexUpdateTransaction.STATUS_ACTIVE;
		attributes = m;
		manager.fireOpen(this);
	}

	/**
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#addItemIterator()
	 */
	public Iterator<SearchBuilderItem> addItemIterator() throws IndexTransactionException
	{
		if ( transactionState != IndexUpdateTransaction.STATUS_ACTIVE) {
			throw new IndexTransactionException("Transaction is not active ");
		}

		return txList.iterator();
	}


	/**
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#close()
	 */
	public void close()
	{
		if ( transactionState != IndexUpdateTransaction.STATUS_NO_TRANSACTION  &&
				transactionState != IndexUpdateTransaction.STATUS_COMMITTED &&
				transactionState != IndexUpdateTransaction.STATUS_UNKNOWN	) {
			try
			{
				rollback();
			}
			catch (IndexTransactionException e)
			{
			}
		}
		transactionState = IndexUpdateTransaction.STATUS_UNKNOWN;
	}
	
	/**
	 * @see org.sakaiproject.search.indexer.api.IndexUpdateTransaction#prepare()
	 */
	public void prepare() throws IndexTransactionException
	{
		if ( transactionState != IndexUpdateTransaction.STATUS_ACTIVE ) {
			throw new IndexTransactionException("Transaction is not active ");
		}
		try
		{
			transactionState = IndexUpdateTransaction.STATUS_PREPARING;
			transactionId = manager.sequence.getNextId();	
			indexWriter.close();
			indexWriter = null;
			saveTransactionList();
			manager.firePrepare(this);
			transactionState = IndexUpdateTransaction.STATUS_PREPARED;
		}
		catch (IOException e)
		{
			throw new IndexTransactionException("Failed to commit ", e);
		}
	}


	/**
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#commit()
	 */
	public void commit() throws IndexTransactionException
	{
		if ( transactionState != IndexUpdateTransaction.STATUS_PREPARED ) {
			throw new IndexTransactionException("Transaction is not prepared ");
		}
		try
		{
			transactionState = IndexUpdateTransaction.STATUS_COMMITTING;
			manager.fireCommit(this);
			FileUtils.deleteAll(tempIndex);
			tempIndex = null;
			transactionId = -1;
			transactionState = IndexUpdateTransaction.STATUS_COMMITTED;
		}
		catch (Exception e)
		{
			throw new IndexTransactionException("Failed to commit ", e);
		}
	}

	/**
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#getIndexWriter()
	 */
	public IndexWriter getIndexWriter() throws IndexTransactionException
	{
		if ( transactionState != IndexUpdateTransaction.STATUS_ACTIVE) {
			throw new IndexTransactionException("Transaction is not active ");
		}
		if (indexWriter == null)
		{
			try
			{
				tempIndex = manager.getTemporarySegment(transactionId);
				indexWriter = new IndexWriter(tempIndex, manager.getAnalyzer(), true);
				indexWriter.setUseCompoundFile(true);
				// indexWriter.setInfoStream(System.out);
				indexWriter.setMaxMergeDocs(50);
				indexWriter.setMergeFactor(50);
			}
			catch (IOException ex)
			{
				throw new IndexTransactionException(
						"Cant Create Transaction Index working space ", ex);
			}
		}
		return indexWriter;
	}

	/**
	 * The name of the temp index shouldbe used to locate the index, and NOT the transaction ID.
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#getTempIndex()
	 */
	public String getTempIndex()
	{
		return tempIndex.getAbsolutePath();
	}

	/**
	 * The transaction ID will change as the status cahnges.
	 * While the transaction is active it will have a local ID, when the transaction is prepared
	 * the cluster wide transaction id will be created. Once prepare has been performed, 
	 * the transaction should be committed
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#getTransactionId()
	 */
	public long getTransactionId()
	{
		
		return transactionId;
	}

	/**
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#rollback()
	 */
	public void rollback() throws IndexTransactionException
	{
		if ( transactionState != IndexUpdateTransaction.STATUS_ACTIVE &&
				transactionState != IndexUpdateTransaction.STATUS_COMMITTING &&
				transactionState != IndexUpdateTransaction.STATUS_MARKED_ROLLBACK &&
				transactionState != IndexUpdateTransaction.STATUS_PREPARED &&
				transactionState != IndexUpdateTransaction.STATUS_PREPARING &&
				transactionState != IndexUpdateTransaction.STATUS_ROLLING_BACK) {
			throw new IndexTransactionException("Transaction cannot be rolled back, state not active, commtting, marked rolled bac, prepared, preparing or rollingback ");
		}
		try
		{
			transactionState = IndexUpdateTransaction.STATUS_ROLLING_BACK;
			indexWriter.close();
			indexWriter = null;
			saveTransactionList();
			manager.fireRollback(this);
			FileUtils.deleteAll(tempIndex);
			tempIndex = null;
			transactionId = -1;
			transactionState = IndexUpdateTransaction.STATUS_ROLLEDBACK;
		}
		catch (IOException e)
		{
		}
		log.info("Transaction Rollback Completed on "+this);
	}
	
	/**
	 * @see org.sakaiproject.search.indexer.api.IndexUpdateTransaction#getStatus()
	 */
	public int getStatus()
	{
		// TODO Auto-generated method stub
		return 0;
	}


	/**
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#setItems(java.util.List)
	 */
	public void setItems(List<SearchBuilderItem> items) throws IndexTransactionException
	{
		if ( transactionState != IndexUpdateTransaction.STATUS_ACTIVE) {
			throw new IndexTransactionException("Transaction is not active ");
		}
		if ( txList != null ) {
			throw new IndexTransactionException("Once the items has been set, it cannot be reset while the transaction is in process");
		}
		itemList  = items;
		txList = new ArrayList<SearchBuilderItem>();
		for (Iterator<SearchBuilderItem> itxList = items.iterator(); itxList.hasNext(); ) {
			SearchBuilderItem sbi = itxList.next();
			if (sbi != null &&
					SearchBuilderItem.STATE_LOCKED.equals(sbi
							.getSearchstate()) &&
					SearchBuilderItem.ACTION_ADD.equals(sbi
									.getSearchaction()) 
					)
			{
				String ref = sbi.getName();
				if ( ref != null ) {
					txList.add(sbi);					
				} else {
					log.warn("Null Reference presented to Index Queue, ignoring ");
				}
			}			
		}		
		if ( txList.size() > 0 ) {
			transactionId = manager.sequence.getNextId();
		} else {
			txList = null;
			throw new NoItemsToIndexException("No Items available to index");
		}
	}
	/**
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#getItems()
	 */
	public List<SearchBuilderItem> getItems()
	{
		return itemList;
	}

	
	
	private void saveTransactionList() throws IOException
	{
		File transactionList = new File(tempIndex, TRANSACTION_LIST);
		DataOutputStream dataOutputStream = new DataOutputStream(
				new FileOutputStream(transactionList));
		for (Iterator<SearchBuilderItem> isbi = txList.iterator(); isbi
				.hasNext();)
		{
			SearchBuilderItem sbi = isbi.next();
			sbi.output(dataOutputStream);
		}
		dataOutputStream.close();

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
	 * @see org.sakaiproject.search.indexer.api.IndexUpdateTransaction#put(java.lang.String, java.lang.Object)
	 */
	public void put(String key, Object obj)
	{
		attributes.put(key, obj);
	}






}
