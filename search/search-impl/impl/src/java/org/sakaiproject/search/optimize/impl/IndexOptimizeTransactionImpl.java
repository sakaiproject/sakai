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

package org.sakaiproject.search.optimize.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.sakaiproject.search.indexer.impl.SearchBuilderItemSerializer;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.optimize.api.IndexOptimizeTransaction;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.transaction.impl.IndexTransactionImpl;
import org.sakaiproject.search.transaction.impl.TransactionManagerImpl;
import org.sakaiproject.search.util.FileUtils;

/**
 * 
 * 
 * @author ieb 
 */
public class IndexOptimizeTransactionImpl extends IndexTransactionImpl implements
		IndexOptimizeTransaction
{

	private static final Log log = LogFactory.getLog(IndexOptimizeTransactionImpl.class);

	private IndexWriter indexWriter;

	private File tempIndex;

	private List<SearchBuilderItem> txList;

	private SearchBuilderItemSerializer searchBuilderItemSerializer = new SearchBuilderItemSerializer();

	/**
	 * @param m
	 * @param impl
	 * @throws IndexTransactionException
	 */
	public IndexOptimizeTransactionImpl(TransactionManagerImpl manager,
			Map<String, Object> m) throws IndexTransactionException
	{
		super(manager, m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.transaction.impl.IndexTransactionImpl#doBeforePrepare()
	 */
	@Override
	protected void doBeforePrepare() throws IndexTransactionException
	{
		try
		{
			transactionId = manager.getSequence().getNextId();
			indexWriter.close();
			indexWriter = null;
			searchBuilderItemSerializer.saveTransactionList(tempIndex, txList);
		}
		catch (Exception ex)
		{
			throw new IndexTransactionException("Failed to prepare transaction", ex);
		}
		super.doBeforePrepare();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.transaction.impl.IndexTransactionImpl#doAfterCommit()
	 */
	@Override
	protected void doAfterCommit() throws IndexTransactionException
	{
		try
		{
			FileUtils.deleteAll(tempIndex);
		}
		catch (Exception e)
		{
			throw new IndexTransactionException("Failed to commit ", e);
		}
		super.doAfterCommit();
	}

	/**
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#getIndexWriter()
	 */
	public IndexWriter getIndexWriter() throws IndexTransactionException
	{
		if (transactionState != IndexTransaction.STATUS_ACTIVE)
		{
			throw new IndexTransactionException("Transaction is not active ");
		}
		if (indexWriter == null)
		{
			try
			{
				tempIndex = ((OptimizeIndexManager) manager)
						.getTemporarySegment(transactionId);
				indexWriter = new IndexWriter(tempIndex, ((OptimizeIndexManager) manager)
						.getAnalyzer(), true);
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
	 * The name of the temp index shouldbe used to locate the index, and NOT the
	 * transaction ID.
	 * 
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#getTempIndex()
	 */
	public String getTempIndex()
	{
		return tempIndex.getAbsolutePath();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.transaction.impl.IndexTransactionImpl#doBeforeRollback()
	 */
	@Override
	protected void doBeforeRollback() throws IndexTransactionException
	{
		try
		{
			indexWriter.close();
			indexWriter = null;
			searchBuilderItemSerializer.saveTransactionList(tempIndex, txList);
		}
		catch (Exception ex)
		{
			throw new IndexTransactionException("Failed to start rollback ", ex);
		}
		super.doBeforeRollback();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.transaction.impl.IndexTransactionImpl#doAfterRollback()
	 */
	@Override
	protected void doAfterRollback() throws IndexTransactionException
	{
		super.doAfterRollback();
		try
		{
			FileUtils.deleteAll(tempIndex);
		}
		catch (Exception ex)
		{
			throw new IndexTransactionException("Failed to complete rollback ", ex);
		}
		tempIndex = null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.optimize.api.IndexOptimizeTransaction#getOptimizableSegments()
	 */
	public File[] getOptimizableSegments()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.optimize.api.IndexOptimizeTransaction#getPermanentIndexWriter()
	 */
	public IndexWriter getPermanentIndexWriter()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.optimize.api.IndexOptimizeTransaction#getTemporaryIndexWriter()
	 */
	public IndexWriter getTemporaryIndexWriter()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.optimize.api.IndexOptimizeTransaction#setOptimizableSegments(java.io.File[])
	 */
	public void setOptimizableSegments(File[] optimzableSegments)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.optimize.api.IndexOptimizeTransaction#setPermanentIndexWriter(org.apache.lucene.index.IndexWriter)
	 */
	public void setPermanentIndexWriter(IndexWriter pw)
	{
		// TODO Auto-generated method stub
		
	}

}
