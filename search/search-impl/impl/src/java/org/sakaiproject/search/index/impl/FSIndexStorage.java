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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sakaiproject.search.api.SearchService;

/**
 * A local only filestore implementation. This is a simple IndexImplementation
 * that performs all its indexing in a single, unoptimized segment on the local
 * filesystem To set the location use
 * location@org.sakaiproject.search.api.SearchService.LocalIndexStorage in
 * sakai.properties Check that the bean name is correct in the spring
 * components.xml file
 * 
 * @author ieb
 */
public class FSIndexStorage extends BaseIndexStorage
{
	private static Log log = LogFactory.getLog(FSIndexStorage.class);

	protected String searchIndexDirectory = "searchindex";

	protected boolean recoverCorruptedIndex = false;

	private long lastUpdate = System.currentTimeMillis();

	public void init()
	{

	}

	public void doPreIndexUpdate() throws IOException
	{
		log.debug("Starting process List on " + searchIndexDirectory);
		File f = new File(searchIndexDirectory);
		if (!f.exists())
		{
			if (!f.mkdirs())
			{
				log.warn("doPreIndexUpdate() couldn't delete " + f.getPath());
			}
			log.debug("Indexing in " + f.getAbsolutePath());
		}

		if (IndexReader.isLocked(searchIndexDirectory))
		{
			// this could be dangerous, I am assuming that
			// the locking mechanism implemented here is
			// robust and
			// already prevents multiple modifiers.
			// A more

			IndexReader.unlock(FSDirectory.getDirectory(searchIndexDirectory, true));
			log.warn("Unlocked Lucene Directory for update, hope this is Ok");
		}
	}

	public IndexReader getIndexReader() throws IOException
	{
		return IndexReader.open(searchIndexDirectory);
	}

	public IndexWriter getIndexWriter(boolean create) throws IOException
	{
		return new IndexWriter(searchIndexDirectory, getAnalyzer(), create);
	}

	public void doPostIndexUpdate() throws IOException
	{
		lastUpdate = System.currentTimeMillis();

	}

	/**
	 * @return Returns the searchIndexDirectory.
	 */
	public String getSearchIndexDirectory()
	{
		return searchIndexDirectory;
	}

	protected IndexSearcher getIndexSearcher() throws IOException
	{
		IndexSearcher indexSearcher = null;
		try
		{
			long reloadStart = System.currentTimeMillis();
			File indexDirectoryFile = new File(searchIndexDirectory);
			if (!indexDirectoryFile.exists())
			{
				if (!indexDirectoryFile.mkdirs())
				{
					log.warn("getIdexSearch couldn't create directory " + indexDirectoryFile.getPath());
				}
			}

			indexSearcher = new IndexSearcher(searchIndexDirectory);
			if (indexSearcher == null)
			{
				log.warn("No search Index exists at this time");

			}
			long reloadEnd = System.currentTimeMillis();
			if (diagnostics)
			{
				log.info("Reload Complete " + indexSearcher.getIndexReader().numDocs()
						+ " in " + (reloadEnd - reloadStart));
			}

		}
		catch (FileNotFoundException e)
		{
			log.error("There has been a major poblem with the"
					+ " Search Index which has become corrupted ", e);
			if (doIndexRecovery())
			{
				indexSearcher = new IndexSearcher(searchIndexDirectory);
			}
		}
		catch (IOException e)
		{
			log.error("There has been a major poblem with the "
					+ "Search Index which has become corrupted", e);
			if (doIndexRecovery())
			{
				indexSearcher = new IndexSearcher(searchIndexDirectory);
			}
		}
		return indexSearcher;
	}

	protected boolean doIndexRecovery() throws IOException
	{
		if (recoverCorruptedIndex)
		{
			IndexWriter iw = getIndexWriter(true);
			Document doc = new Document();
			String message = "Index Recovery performed on " + (new Date()).toString();
			doc.add(new Field(SearchService.FIELD_CONTENTS, message, Field.Store.NO,
					Field.Index.TOKENIZED));
			iw.addDocument(doc);
			iw.close();
			log.error("Sucess fully recoverd From a corrupted index, "
					+ "the index will be empty and require a " + "complete rebuild");
			return true;
		}
		return false;
	}

	public boolean indexExists()
	{
		return IndexReader.indexExists(searchIndexDirectory);
	}

	/**
	 * @return Returns the recoverCorruptedIndex.
	 */
	public boolean isRecoverCorruptedIndex()
	{
		return recoverCorruptedIndex;
	}

	/**
	 * @param recoverCorruptedIndex
	 *        The recoverCorruptedIndex to set.
	 */
	public void setRecoverCorruptedIndex(boolean recoverCorruptedIndex)
	{
		log.info("Using FSIndexStorage, storing the index "
				+ "on the local file system in " + searchIndexDirectory
				+ " if the index is corrupted recovery will "
				+ (recoverCorruptedIndex ? "" : "NOT ") + " be automatic");
		this.recoverCorruptedIndex = recoverCorruptedIndex;
	}

	public void setLocation(String location)
	{
		searchIndexDirectory = location;

	}

	public long getLastUpdate()
	{
		// not really relevant in the non cluster environment
		return lastUpdate;
	}

	public List getSegmentInfoList()
	{
		List<Object[]> l = new ArrayList<Object[]>();
		l
				.add(new Object[] {
						"Index Segment Info is not implemented for Local file system index stores",
						"", "" });
		return l;
	}

	public void closeIndexReader(IndexReader indexReader) throws IOException
	{
		if (indexReader != null)
		{
			indexReader.close();
		}
	}

	public void closeIndexWriter(IndexWriter indexWrite) throws IOException
	{
		if (indexWrite != null)
		{
			indexWrite.close();
		}

	}

	public boolean isMultipleIndexers()
	{
		return false;
	}

	public void closeIndexSearcher(IndexSearcher indexSearcher)
	{
		IndexReader indexReader = indexSearcher.getIndexReader();
		boolean closedAlready = false;
		try
		{
			if (indexReader != null)
			{
				indexReader.close();
				closedAlready = true;
			}
		}
		catch (Exception ex)
		{
			log.error("Failed to close Index Reader " + ex.getMessage());
		}
		try
		{
			indexSearcher.close();
		}
		catch (Exception ex)
		{
			if (closedAlready)
			{
				log.debug("Failed to close Index Searcher " + ex.getMessage());
			}
			else
			{
				log.error("Failed to close Index Searcher " + ex.getMessage());
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#centralIndexExists()
	 */
	public boolean centralIndexExists()
	{
		return indexExists();
	}

	private Directory spellDirectory = null;
	public Directory getSpellDirectory() {
		return spellDirectory;
	}

}
