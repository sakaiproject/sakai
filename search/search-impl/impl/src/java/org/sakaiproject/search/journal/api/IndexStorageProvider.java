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

package org.sakaiproject.search.journal.api;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

/**
 * @author ieb
 */
public interface IndexStorageProvider
{

	/**
	 * @return
	 * @throws IOException
	 */
	IndexSearcher getIndexSearcher() throws IOException;

	/**
	 * @return
	 */
	long getLastLoad();

	/**
	 * @return
	 */
	long getLastLoadTime();

	/**
	 * @return
	 */
	long getLastUpdate();

	/**
	 * @return
	 */
	List getSegmentInfoList();

	/**
	 * @return
	 */
	Analyzer getAnalyzer();

	/**
	 * @param indexListener
	 */
	void addIndexListener(IndexListener indexListener);
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public IndexReader getIndexReader()  throws IOException;

	/**
	 * Get the SpellIndexDirectory
	 * @return
	 */
	public Directory getSpellDirectory();
}
