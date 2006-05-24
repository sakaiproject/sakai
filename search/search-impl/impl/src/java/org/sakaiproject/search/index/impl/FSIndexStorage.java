/**
 * 
 */
package org.sakaiproject.search.index.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.index.AnalyzerFactory;
import org.sakaiproject.search.index.IndexStorage;

public class FSIndexStorage implements IndexStorage
{
	private static Log log = LogFactory.getLog(FSIndexStorage.class);

	protected String searchIndexDirectory = "searchindex";

	protected AnalyzerFactory analyzerFactory = null;

	protected boolean recoverCorruptedIndex = false;

	public void doPreIndexUpdate() throws IOException
	{
		log.debug("Starting process List on " + searchIndexDirectory);
		File f = new File(searchIndexDirectory);
		if (!f.exists())
		{
			f.mkdirs();
			log.debug("Indexing in " + f.getAbsolutePath());
		}

		if (IndexReader.isLocked(searchIndexDirectory))
		{
			// this could be dangerous, I am assuming that
			// the locking mechanism implemented here is
			// robust and
			// already prevents multiple modifiers.
			// A more

			IndexReader.unlock(FSDirectory.getDirectory(searchIndexDirectory,
					true));
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

	}

	/**
	 * @return Returns the searchIndexDirectory.
	 */
	public String getSearchIndexDirectory()
	{
		return searchIndexDirectory;
	}

	/**
	 * @param searchIndexDirectory
	 *        The searchIndexDirectory to set.
	 */
	public void setSearchIndexDirectory(String searchIndexDirectory)
	{
		this.searchIndexDirectory = searchIndexDirectory;
	}

	public IndexSearcher getIndexSearcher() throws IOException
	{
		IndexSearcher indexSearcher = null;
		try
		{
			long reloadStart = System.currentTimeMillis();
			File indexDirectoryFile = new File(searchIndexDirectory);
			if (!indexDirectoryFile.exists())
			{
				indexDirectoryFile.mkdirs();
			}

			indexSearcher = new IndexSearcher(searchIndexDirectory);
			if (indexSearcher == null)
			{
				log.warn("No search Index exists at this time");

			}
			long reloadEnd = System.currentTimeMillis();
			log.info("Reload Complete " + indexSearcher.maxDoc() + " in "
					+ (reloadEnd - reloadStart));

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
			String message = "Index Recovery performed on "
					+ (new Date()).toString();
			doc.add(new Field(SearchService.FIELD_CONTENTS, message, Field.Store.NO,
					Field.Index.TOKENIZED));
			iw.addDocument(doc);
			iw.close();
			log.error("Sucess fully recoverd From a corrupted index, "
					+ "the index will be empty and require a "
					+ "complete rebuild");
			return true;
		}
		return false;
	}

	public boolean indexExists()
	{
		return IndexReader.indexExists(searchIndexDirectory);
	}

	/**
	 * @return Returns the analyzerFactory.
	 */
	public AnalyzerFactory getAnalyzerFactory()
	{
		return analyzerFactory;
	}

	/**
	 * @param analyzerFactory
	 *        The analyzerFactory to set.
	 */
	public void setAnalyzerFactory(AnalyzerFactory analyzerFactory)
	{
		this.analyzerFactory = analyzerFactory;
	}

	public Analyzer getAnalyzer()
	{
		return analyzerFactory.newAnalyzer();
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

}
