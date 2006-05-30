/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.search.index;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

/**
 * Defines the IndexStorage mechanism used
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
