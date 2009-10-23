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

package org.sakaiproject.search.index.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.sakaiproject.search.index.IndexReloadListener;
import org.sakaiproject.search.index.IndexStorage;

/**
 * A simple multiplex class to enable configuration of the storage mecahnism in
 * sakai.properties to use
 * indexStorageName@org.sakaiproject.search.index.IndexStorage = filesystem
 * indexStorageName@org.sakaiproject.search.index.IndexStorage = cluster
 * indexStorageName@org.sakaiproject.search.index.IndexStorage = db
 * recoverCorruptedIndex@org.sakaiproject.search.index.IndexStorage = false may
 * cahnge, and it is worth looking in the components for the real values.
 * 
 * @author ieb
 */
public class SearchIndexStorage implements IndexStorage
{
	private final static Log log = LogFactory.getLog(SearchIndexStorage.class);

	private IndexStorage runningIndexStorage = null;

	private Map currentStores = null;

	private IndexStorage defaultIndexStorage;

	private String indexStorageName;

	private boolean recover;

	private String location;

	public void init()
	{
		log.info("init()");
		try
		{
			runningIndexStorage = (IndexStorage) currentStores.get(indexStorageName
					.trim());

			if (runningIndexStorage == null)
			{
				runningIndexStorage = defaultIndexStorage;
			}
		}
		catch (Exception ex)
		{
			log.warn("Failed to init SearchIndexStorage ", ex);
		}
		log.info("init() Ok");
	}

	public IndexReader getIndexReader() throws IOException
	{
		return runningIndexStorage.getIndexReader();
	}

	public IndexWriter getIndexWriter(boolean create) throws IOException
	{
		return runningIndexStorage.getIndexWriter(create);
	}

	public void doPostIndexUpdate() throws IOException
	{
		runningIndexStorage.doPostIndexUpdate();
	}

	public void doPreIndexUpdate() throws IOException
	{
		runningIndexStorage.doPreIndexUpdate();
	}

	public boolean indexExists()
	{
		return runningIndexStorage.indexExists();
	}

	public Analyzer getAnalyzer()
	{
		return runningIndexStorage.getAnalyzer();
	}

	/**
	 * @return Returns the currentStores.
	 */
	public Map getCurrentStores()
	{
		return currentStores;
	}

	/**
	 * @param currentStores
	 *        The currentStores to set.
	 */
	public void setCurrentStores(Map currentStores)
	{
		this.currentStores = currentStores;
	}

	/**
	 * @return Returns the defaultIndexStorage.
	 */
	public IndexStorage getDefaultIndexStorage()
	{
		return defaultIndexStorage;
	}

	/**
	 * @param defaultIndexStorage
	 *        The defaultIndexStorage to set.
	 */
	public void setDefaultIndexStorage(IndexStorage defaultIndexStorage)
	{
		this.defaultIndexStorage = defaultIndexStorage;
	}

	/**
	 * @return Returns the indexStorageName.
	 */
	public String getIndexStorageName()
	{
		return indexStorageName;
	}

	/**
	 * @param indexStorageName
	 *        The indexStorageName to set.
	 */
	public void setIndexStorageName(String indexStorageName)
	{
		this.indexStorageName = indexStorageName;
	}

	/**
	 * @return Returns the runningIndexStorage.
	 */
	public IndexStorage getRunningIndexStorage()
	{
		return runningIndexStorage;
	}

	/**
	 * @param runningIndexStorage
	 *        The runningIndexStorage to set.
	 */
	public void setRunningIndexStorage(IndexStorage runningIndexStorage)
	{
		this.runningIndexStorage = runningIndexStorage;
	}

	public void setRecoverCorruptedIndex(boolean recover)
	{
		this.recover = recover;

	}

	public long getLastUpdate()
	{
		return runningIndexStorage.getLastUpdate();
	}

	public List getSegmentInfoList()
	{
		return runningIndexStorage.getSegmentInfoList();
	}

	public void closeIndexReader(IndexReader indexReader) throws IOException
	{
		runningIndexStorage.closeIndexReader(indexReader);
	}

	public void closeIndexWriter(IndexWriter indexWrite) throws IOException
	{
		runningIndexStorage.closeIndexWriter(indexWrite);
	}

	public boolean isMultipleIndexers()
	{
		return runningIndexStorage.isMultipleIndexers();
	}

	public void closeIndexSearcher(IndexSearcher indexSearcher)
	{
		runningIndexStorage.closeIndexSearcher(indexSearcher);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#disableDiagnostics()
	 */
	public void disableDiagnostics()
	{
		runningIndexStorage.disableDiagnostics();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#enableDiagnostics()
	 */
	public void enableDiagnostics()
	{
		runningIndexStorage.enableDiagnostics();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#hasDiagnostics()
	 */
	public boolean hasDiagnostics()
	{
		return runningIndexStorage.hasDiagnostics();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#centralIndexExists()
	 */
	public boolean centralIndexExists()
	{
		return runningIndexStorage.centralIndexExists();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#addReloadListener(org.sakaiproject.search.index.IndexReloadListener)
	 */
	public void addReloadListener(IndexReloadListener indexReloadListener)
	{
		runningIndexStorage.addReloadListener(indexReloadListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#forceNextReload()
	 */
	public void forceNextReload()
	{
		runningIndexStorage.forceNextReload();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getIndexSearcher(boolean)
	 */
	public IndexSearcher getIndexSearcher(boolean reload) throws IOException
	{
		return runningIndexStorage.getIndexSearcher(reload);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getLastLoad()
	 */
	public long getLastLoad()
	{
		return runningIndexStorage.getLastLoad();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getLastLoadTime()
	 */
	public long getLastLoadTime()
	{
		return runningIndexStorage.getLastLoadTime();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#removeReloadListener(org.sakaiproject.search.index.IndexReloadListener)
	 */
	public void removeReloadListener(IndexReloadListener indexReloadListener)
	{
		runningIndexStorage.removeReloadListener(indexReloadListener);

	}

	public Directory getSpellDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

}
