/**
 * 
 */
package org.sakaiproject.search.index;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

/**
 * @author ieb
 *
 */
public interface IndexStorage
{
	
	/**
	 * get an Index Reader for the IndexStorage type
	 * @return
	 * @throws IOException
	 */
	IndexReader getIndexReader() throws IOException;

	/**
	 * get an index writer, and create if asked to
	 * @param create
	 * @return
	 * @throws IOException
	 */
	IndexWriter getIndexWriter(boolean create) throws IOException;
	
	/**
	 * get an index searcher
	 * @return
	 * @throws IOException
	 */
	IndexSearcher getIndexSearcher() throws IOException;

	/**
	 * perform all operations necessary after an update cycle
	 * @throws IOException
	 */
	void doPostIndexUpdate() throws IOException;
	
	/**
	 * perform all operations before an update cycle
	 * @throws IOException
	 */
	void doPreIndexUpdate() throws IOException;
	
	

	/**
	 * Does the index exist
	 * @return
	 */
	boolean indexExists();
	
	/**
	 * Get an analyzer correct for the indexer being used.
	 * @return
	 */
	Analyzer getAnalyzer();

	/**
	 * if set to true the IndexStorageWill automatically attempt to recover a corrupted index
	 * Not all IndexStorage implementations can do this, 
	 *
	 */
	void setRecoverCorruptedIndex(boolean recover);
	
	/**
	 * The location of the storage
	 * @param location
	 */
	void setLocation(String location);

}
