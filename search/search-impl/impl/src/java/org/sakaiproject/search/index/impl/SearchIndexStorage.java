/**
 * 
 */
package org.sakaiproject.search.index.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.search.index.IndexStorage;

/**
 * A simple multiplex class to enable configuration of the storage mecahnism in sakai.properties
 * to use
 * 
 * indexStorageName@org.sakaiproject.search.index.IndexStorage = filesystem
 * indexStorageName@org.sakaiproject.search.index.IndexStorage = cluster
 * indexStorageName@org.sakaiproject.search.index.IndexStorage = db
 * recoverCorruptedIndex@org.sakaiproject.search.index.IndexStorage = false
 * location@org.sakaiproject.search.index.IndexStorage = tableName|localDirectory
 * Default is to use the local filesystem
 * These values may cahnge, and it is worth looking in the components for the real values.
 * @author ieb
 *
 */
public class SearchIndexStorage implements IndexStorage
{
	private IndexStorage runningIndexStorage = null;
	private Map currentStores = null;
	private IndexStorage defaultIndexStorage;
	private String indexStorageName;
	private boolean recover;
	private String location;

	public void init() {
		runningIndexStorage = (IndexStorage)currentStores.get(indexStorageName);
		if ( runningIndexStorage == null ) {
			runningIndexStorage = defaultIndexStorage;
		}
		if ( location != null ) {
			runningIndexStorage.setLocation(location);
		}
		runningIndexStorage.setRecoverCorruptedIndex(recover);
	}

	public IndexReader getIndexReader() throws IOException
	{
		return runningIndexStorage.getIndexReader();
	}

	public IndexWriter getIndexWriter(boolean create) throws IOException
	{
		return runningIndexStorage.getIndexWriter(create);
	}

	public IndexSearcher getIndexSearcher() throws IOException
	{
		return runningIndexStorage.getIndexSearcher();
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
	 * @param currentStores The currentStores to set.
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
	 * @param defaultIndexStorage The defaultIndexStorage to set.
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
	 * @param indexStorageName The indexStorageName to set.
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
	 * @param runningIndexStorage The runningIndexStorage to set.
	 */
	public void setRunningIndexStorage(IndexStorage runningIndexStorage)
	{
		this.runningIndexStorage = runningIndexStorage;
	}

	public void setRecoverCorruptedIndex(boolean recover)
	{
		this.recover  = recover;
		
	}

	public void setLocation(String location)
	{
		this.location = location;
		
	}

}
