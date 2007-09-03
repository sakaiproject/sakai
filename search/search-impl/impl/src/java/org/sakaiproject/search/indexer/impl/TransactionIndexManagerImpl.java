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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.sakaiproject.search.index.AnalyzerFactory;
import org.sakaiproject.search.indexer.api.IndexUpdateTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.transaction.impl.TransactionManagerImpl;

/**
 * @author ieb
 */
public class TransactionIndexManagerImpl extends  TransactionManagerImpl 
{
	private static final Log log = LogFactory.getLog(TransactionIndexManagerImpl.class);

	protected static final String TEMP_INDEX_NAME = "indextx-";


	/**
	 * dependency
	 * The token analyzer
	 */
	private AnalyzerFactory analyzerFactory = null;

	/**
	 * dependency
	 */
	private String searchIndexWorkingDirectory;

	

	/**
	 * Does nothing at the moment.
	 *
	 */
	public void init() {
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.service.index.transactional.api.TransactionIndexManager#openTransaction(java.util.List)
	 */
	public IndexUpdateTransaction openTransaction(Map<String, Object> m) throws IndexTransactionException
	{
		
		return new IndexUpdateTransactionImpl(this,m);

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
		f = new File(searchIndexWorkingDirectory, TEMP_INDEX_NAME + txid);
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

	/**
	 * @return the searchIndexWorkingDirectory
	 */
	public String getSearchIndexWorkingDirectory()
	{
		return searchIndexWorkingDirectory;
	}

	/**
	 * @param searchIndexWorkingDirectory
	 *        the searchIndexWorkingDirectory to set
	 */
	public void setSearchIndexWorkingDirectory(String searchIndexWorkingDirectory)
	{
		this.searchIndexWorkingDirectory = searchIndexWorkingDirectory;
	}



}
