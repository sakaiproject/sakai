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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.sakaiproject.search.index.AnalyzerFactory;
import org.sakaiproject.search.indexer.api.IndexUpdateTransaction;
import org.sakaiproject.search.indexer.api.IndexUpdateTransactionListener;
import org.sakaiproject.search.journal.impl.JournalSettings;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.transaction.api.TransactionListener;
import org.sakaiproject.search.transaction.impl.TransactionManagerImpl;

/**
 * @author ieb Unit test
 * @see org.sakaiproject.search.indexer.impl.test.TransactionalIndexWorkerTest
 */
public class TransactionIndexManagerImpl extends TransactionManagerImpl
{
	private static final Log log = LogFactory.getLog(TransactionIndexManagerImpl.class);

	protected static final String TEMP_INDEX_NAME = "indextx-";

	/**
	 * dependency The token analyzer
	 */
	private AnalyzerFactory analyzerFactory = null;

	/**
	 * dependency
	 */
	private JournalSettings journalSettings;

	public void init()
	{

	}

	public void destroy()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.service.index.transactional.api.TransactionIndexManager#openTransaction(java.util.List)
	 */
	public IndexUpdateTransaction openTransaction(Map<String, Object> m)
			throws IndexTransactionException
	{

		IndexUpdateTransaction it = new IndexUpdateTransactionImpl(this,journalSettings, m);
		it.open();
		return it;

	}

	/**
	 * @return
	 * @throws IOException
	 */
	protected File getTemporarySegment(long txid) throws IOException
	{
		// this index will not have a timestamp, and hence will not be part sync
		// with the db
		File f = null;
		f = new File(journalSettings.getIndexerWorkingDirectory(), TEMP_INDEX_NAME + txid);
		if (f.exists())
		{
			throw new IOException("Failed to create index transaction working space ");
		}
		f.mkdirs();
		return f;
	}

	public Analyzer getAnalyzer()
	{
		return analyzerFactory.newAnalyzer();
	}

	/**
	 * @return the analyzerFactory
	 */
	public AnalyzerFactory getAnalyzerFactory()
	{
		return analyzerFactory;
	}

	/**
	 * @param analyzerFactory
	 *        the analyzerFactory to set
	 */
	public void setAnalyzerFactory(AnalyzerFactory analyzerFactory)
	{
		this.analyzerFactory = analyzerFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.transaction.impl.TransactionManagerImpl#addTransactionListener(org.sakaiproject.search.transaction.api.TransactionListener)
	 */
	@Override
	public void addTransactionListener(TransactionListener transactionListener)
	{
		if (transactionListener instanceof IndexUpdateTransactionListener)
		{
			super.addTransactionListener(transactionListener);
		}
		else
		{
			throw new RuntimeException(
					"transactionListener must implement JournalTransactionListener "
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
			if (!(tl instanceof IndexUpdateTransactionListener))
			{
				throw new RuntimeException(
						"transactionListener must implement JournalTransactionListener "
								+ tl);
			}
		}
		super.setTransactionListeners(transactionListeners);
	}

	/**
	 * @return the journalSettings
	 */
	public JournalSettings getJournalSettings()
	{
		return journalSettings;
	}

	/**
	 * @param journalSettings
	 *        the journalSettings to set
	 */
	public void setJournalSettings(JournalSettings journalSettings)
	{
		this.journalSettings = journalSettings;
	}

}
