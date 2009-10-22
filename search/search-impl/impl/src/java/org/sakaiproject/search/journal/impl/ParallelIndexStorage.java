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

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.search.index.IndexReloadListener;
import org.sakaiproject.search.index.IndexStorage;
import org.sakaiproject.search.journal.api.IndexStorageProvider;

/**
 * @author ieb
 */
public class ParallelIndexStorage implements IndexStorage
{

	private static final Log log = LogFactory.getLog(ParallelIndexStorage.class);

	private IndexStorageProvider indexStorageProvider;

	public void init()
	{

	}

	public void destroy()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#addReloadListener(org.sakaiproject.search.index.IndexReloadListener)
	 */
	public void addReloadListener(IndexReloadListener indexReloadListener)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#centralIndexExists()
	 */
	public boolean centralIndexExists()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#closeIndexReader(org.apache.lucene.index.IndexReader)
	 */
	public void closeIndexReader(IndexReader indexReader) throws IOException
	{
		throw new UnsupportedOperationException("IndexReader may not be closed  ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#closeIndexSearcher(org.apache.lucene.search.IndexSearcher)
	 */
	public void closeIndexSearcher(IndexSearcher oldRunningIndexSearcher)
	{
		throw new UnsupportedOperationException("IndexSercher may not be closed  ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#closeIndexWriter(org.apache.lucene.index.IndexWriter)
	 */
	public void closeIndexWriter(IndexWriter indexWrite) throws IOException
	{
		throw new UnsupportedOperationException("IndexWriter may not be closed  ");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#doPostIndexUpdate()
	 */
	public void doPostIndexUpdate() throws IOException
	{
		throw new UnsupportedOperationException("No Index Updates allowed  ");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#doPreIndexUpdate()
	 */
	public void doPreIndexUpdate() throws IOException
	{
		throw new UnsupportedOperationException("No Index Updates allowed  ");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#forceNextReload()
	 */
	public void forceNextReload()
	{
		log.warn("Parallel Index Reader does not support a reload operation ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getAnalyzer()
	 */
	public Analyzer getAnalyzer()
	{
		return indexStorageProvider.getAnalyzer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getIndexReader()
	 */
	public IndexReader getIndexReader() throws IOException
	{
		
		return indexStorageProvider.getIndexReader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getIndexSearcher(boolean)
	 */
	public IndexSearcher getIndexSearcher(boolean reload) throws IOException
	{
		return indexStorageProvider.getIndexSearcher();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getIndexWriter(boolean)
	 */
	public IndexWriter getIndexWriter(boolean create) throws IOException
	{
		throw new UnsupportedOperationException("Index Writer is not available ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getLastLoad()
	 */
	public long getLastLoad()
	{
		return indexStorageProvider.getLastLoad();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getLastLoadTime()
	 */
	public long getLastLoadTime()
	{
		return indexStorageProvider.getLastLoadTime();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getLastUpdate()
	 */
	public long getLastUpdate()
	{
		return indexStorageProvider.getLastUpdate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getSegmentInfoList()
	 */
	public List getSegmentInfoList()
	{
		return indexStorageProvider.getSegmentInfoList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#indexExists()
	 */
	public boolean indexExists()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#isMultipleIndexers()
	 */
	public boolean isMultipleIndexers()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#removeReloadListener(org.sakaiproject.search.index.IndexReloadListener)
	 */
	public void removeReloadListener(IndexReloadListener indexReloadListener)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#setRecoverCorruptedIndex(boolean)
	 */
	public void setRecoverCorruptedIndex(boolean recover)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#disableDiagnostics()
	 */
	public void disableDiagnostics()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#enableDiagnostics()
	 */
	public void enableDiagnostics()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#hasDiagnostics()
	 */
	public boolean hasDiagnostics()
	{
		return false;
	}

	/**
	 * @return the indexStorageProvider
	 */
	public IndexStorageProvider getIndexStorageProvider()
	{
		return indexStorageProvider;
	}

	/**
	 * @param indexStorageProvider
	 *        the indexStorageProvider to set
	 */
	public void setIndexStorageProvider(IndexStorageProvider indexStorageProvider)
	{
		this.indexStorageProvider = indexStorageProvider;
	}

}
