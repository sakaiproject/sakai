/**
 * 
 */
package org.sakaiproject.search.index.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.sakaiproject.search.index.IndexStorage;

public class FSIndexStorage implements IndexStorage
{
	private static Log log = LogFactory.getLog(FSIndexStorage.class);
	
	private String searchIndexDirectory = "searchindex";

	public void doPreIndexUpdate() throws IOException
	{
		log.debug("Starting process List on "
				+ searchIndexDirectory);
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

	public IndexWriter getIndexWriter(boolean create) throws IOException {
		return new IndexWriter(
				searchIndexDirectory,
				new StandardAnalyzer(), create);
	}
	
	public void doPostIndexUpdate() throws IOException {
		
	}

	/**
	 * @return Returns the searchIndexDirectory.
	 */
	public String getSearchIndexDirectory()
	{
		return searchIndexDirectory;
	}

	/**
	 * @param searchIndexDirectory The searchIndexDirectory to set.
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

			indexSearcher = new IndexSearcher(
					searchIndexDirectory);
			if (indexSearcher == null)
			{
				log.warn("No search Index exists at this time");
				
			}
			long reloadEnd = System.currentTimeMillis();
			log.info("Reload Complete " + indexSearcher.maxDoc()
					+ " in " + (reloadEnd - reloadStart));

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return indexSearcher;
	}

	public boolean indexExists()
	{
		return IndexReader.indexExists(searchIndexDirectory);
	}


}
