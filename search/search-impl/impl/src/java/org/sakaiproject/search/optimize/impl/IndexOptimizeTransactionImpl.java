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

package org.sakaiproject.search.optimize.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.sakaiproject.search.journal.impl.JournalSettings;
import org.sakaiproject.search.optimize.api.IndexOptimizeTransaction;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.transaction.impl.IndexTransactionImpl;
import org.sakaiproject.search.transaction.impl.TransactionManagerImpl;
import org.sakaiproject.search.util.FileUtils;

/**
 * A transaction holder for Index Optimizations
 * 
 * @author ieb
 */
public class IndexOptimizeTransactionImpl extends IndexTransactionImpl implements
		IndexOptimizeTransaction
{

	private static final Log log = LogFactory.getLog(IndexOptimizeTransactionImpl.class);

	/**
	 * The temporary idnex writer
	 */
	private IndexWriter indexWriter;

	/**
	 * A temporta index directory to build the merge item
	 */
	private File tempIndex;

	/**
	 * The permanent index writer being used for the final merge on commit
	 */
	private IndexWriter permanentIndexWriter;

	/**
	 * The list of segments associated with the transaction
	 */
	private File[] optimizableSegments;

	private JournalSettings journalSettings;

	/**
	 * @param m
	 * @param impl
	 * @throws IndexTransactionException
	 */
	public IndexOptimizeTransactionImpl(TransactionManagerImpl manager, JournalSettings journalSettings,
			Map<String, Object> m) throws IndexTransactionException
	{
		super(manager, m);
		this.journalSettings = journalSettings;
	}

	/**
	 * @see org.sakaiproject.search.transaction.impl.IndexTransactionImpl#doAfterRollback()
	 */
	@Override
	protected void doAfterRollback() throws IndexTransactionException
	{
		try
		{
			if (indexWriter != null)
			{
				indexWriter.close();
			}
			indexWriter = null;
			if (tempIndex != null)
			{
				FileUtils.deleteAll(tempIndex);
			}
		}
		catch (Exception ex)
		{
			throw new IndexTransactionException("Failed to complete rollback ", ex);
		}
		super.doAfterRollback();
		tempIndex = null;
		indexWriter = null;
	}

	/**
	 * @see org.sakaiproject.search.transaction.impl.IndexTransactionImpl#doAfterCommit()
	 */
	@Override
	protected void doAfterCommit() throws IndexTransactionException
	{
		try
		{
			indexWriter.close();
			indexWriter = null;
			FileUtils.deleteAll(tempIndex);
		}
		catch (Exception e)
		{
			throw new IndexTransactionException("Failed to commit ", e);
		}
		super.doAfterCommit();
		tempIndex = null;
	}

	/**
	 * get an index writer based on the transaction ID. This is temporary
	 * segment into which other transient segments will be merged.
	 * 
	 * @see org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction#getIndexWriter()
	 */
	public IndexWriter getIndexWriter() throws IndexTransactionException
	{
		if (transactionState == IndexTransaction.STATUS_COMMITTED
				|| transactionState == IndexTransaction.STATUS_ROLLEDBACK)
		{
			throw new IndexTransactionException("Transaction is not active: State is "
					+ IndexTransaction.TRANSACTION_STATUS[transactionState]);
		}
		if (indexWriter == null)
		{
			if (!IndexTransaction.TRANSACTION_ACTIVE[transactionState])
			{
				throw new IndexTransactionException(
						"Transaction is not active: State is "
								+ IndexTransaction.TRANSACTION_STATUS[transactionState]);
			}
			try
			{
				tempIndex = ((OptimizeIndexManager) manager)
						.getTemporarySegment(transactionId);
				indexWriter = new IndexWriter(tempIndex, ((OptimizeIndexManager) manager)
						.getAnalyzer(), true);
				indexWriter.setUseCompoundFile(true);
				// indexWriter.setInfoStream(System.out);
				indexWriter.setMaxMergeDocs(journalSettings.getLocalMaxMergeDocs());
				indexWriter.setMaxBufferedDocs(journalSettings.getLocalMaxBufferedDocs());
				indexWriter.setMergeFactor(journalSettings.getLocalMaxMergeFactor());
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
	 * @see org.sakaiproject.search.optimize.api.IndexOptimizeTransaction#getOptimizableSegments()
	 */
	public File[] getOptimizableSegments()
	{
		return optimizableSegments;
	}

	/**
	 * @see org.sakaiproject.search.optimize.api.IndexOptimizeTransaction#getPermanentIndexWriter()
	 */
	public IndexWriter getPermanentIndexWriter()
	{
		return permanentIndexWriter;
	}

	/**
	 * @see org.sakaiproject.search.optimize.api.IndexOptimizeTransaction#getTemporaryIndexWriter()
	 */
	public IndexWriter getTemporaryIndexWriter() throws IndexTransactionException
	{
		return getIndexWriter();
	}

	/**
	 * @see org.sakaiproject.search.optimize.api.IndexOptimizeTransaction#setOptimizableSegments(java.io.File[])
	 */
	public void setOptimizableSegments(File[] optimzableSegments)
	{
		this.optimizableSegments = optimzableSegments;

	}

	/**
	 * @see org.sakaiproject.search.optimize.api.IndexOptimizeTransaction#setPermanentIndexWriter(org.apache.lucene.index.IndexWriter)
	 */
	public void setPermanentIndexWriter(IndexWriter pw)
	{
		permanentIndexWriter = pw;

	}

}
