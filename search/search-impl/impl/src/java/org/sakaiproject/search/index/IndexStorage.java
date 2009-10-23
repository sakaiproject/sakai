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

package org.sakaiproject.search.index;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.sakaiproject.search.api.Diagnosable;

/**
 * Defines the IndexStorage mechanism used
 * 
 * @author ieb
 */
public interface IndexStorage extends Diagnosable
{

	/**
	 * get an Index Reader for the IndexStorage type
	 * 
	 * @return
	 * @throws IOException
	 */
	IndexReader getIndexReader() throws IOException;

	/**
	 * get an index writer, and create if asked to
	 * 
	 * @param create
	 * @return
	 * @throws IOException
	 */
	IndexWriter getIndexWriter(boolean create) throws IOException;

	/**
	 * get an index searcher
	 * 
	 * @param reload
	 *        force a reload of the searcher, if the implementation is caching a
	 *        searcher
	 * @return
	 * @throws IOException
	 */
	IndexSearcher getIndexSearcher(boolean reload) throws IOException;

	/**
	 * perform all operations necessary after an update cycle
	 * 
	 * @throws IOException
	 */
	void doPostIndexUpdate() throws IOException;

	/**
	 * perform all operations before an update cycle
	 * 
	 * @throws IOException
	 */
	void doPreIndexUpdate() throws IOException;

	/**
	 * Does the index exist
	 * 
	 * @return
	 */
	boolean indexExists();

	/**
	 * Get an analyzer correct for the indexer being used.
	 * 
	 * @return
	 */
	Analyzer getAnalyzer();

	/**
	 * if set to true the IndexStorageWill automatically attempt to recover a
	 * corrupted index Not all IndexStorage implementations can do this,
	 */
	void setRecoverCorruptedIndex(boolean recover);

	long getLastUpdate();

	List getSegmentInfoList();

	/**
	 * This will close the index reader and release any locks
	 * 
	 * @param indexReader
	 * @throws IOException
	 */
	void closeIndexReader(IndexReader indexReader) throws IOException;

	/**
	 * this will close the index reader and release any locks
	 * 
	 * @param indexWrite
	 * @throws IOException
	 */
	void closeIndexWriter(IndexWriter indexWrite) throws IOException;

	/**
	 * Returns true if its ok to allow multiple indexers to run at the same time
	 * The index storage may manage its own locks.
	 * 
	 * @return
	 */
	boolean isMultipleIndexers();

	void closeIndexSearcher(IndexSearcher oldRunningIndexSearcher);

	/**
	 * A fast method that checks if the index exists in the cluster without
	 * opening or loading the index. Is local, looking on local disk is enough.
	 * 
	 * @return
	 */
	boolean centralIndexExists();

	/**
	 * When the index was last loaded
	 * 
	 * @return
	 */
	long getLastLoad();

	/**
	 * The ammount of time (ms) that it took to load the index last time
	 * 
	 * @return
	 */
	long getLastLoadTime();

	/**
	 * @param indexReloadListener
	 */
	void addReloadListener(IndexReloadListener indexReloadListener);

	/**
	 * @param indexReloadListener
	 */
	void removeReloadListener(IndexReloadListener indexReloadListener);

	/**
	 * 
	 */
	void forceNextReload();
	
	/**
	 * get the spell index directory
	 * @return
	 */
	Directory getSpellDirectory();

}
