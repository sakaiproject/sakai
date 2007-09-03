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

package org.sakaiproject.search.journal.impl;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.search.index.impl.BaseIndexStorage;
import org.sakaiproject.search.journal.api.JournalManager;
import org.sakaiproject.search.journal.api.JournaledIndex;
import org.sakaiproject.search.util.FileUtils;

/**
 * This is a Journaled version of the local FSIndexStorage. It will merge in new
 * versions from the jorunal. This is going to be performed in a non
 * transactional way for the moment.
 * 
 * @author ieb
 */
public class JournaledFSIndexStorage extends BaseIndexStorage implements JournaledIndex
{

	private static final Log log = LogFactory.getLog(JournaledFSIndexStorage.class);

	private ServerConfigurationService serverConfigurationService;

	private String serverId;

	private DataSource datasource;

	private ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock(true);

	private ThreadLocal<Long> lastJournalEntryHolder = new ThreadLocal<Long>();

	private JournalManager journalMonitor;

	private String workingSpace;

	private String searchIndexDirectory;

	private List<File> segments = new ArrayList<File>();

	private long lastUpdate;

	/**
	 * @see org.sakaiproject.search.index.impl.FSIndexStorage#init()
	 */
	public void init()
	{
		serverId = serverConfigurationService.getServerId();
	}

	/**
	 * @see org.sakaiproject.search.maintanence.impl.JournaledObject#getJounalVersion()
	 */
	public long getJournalVersion()
	{
		long version = -1;
		Connection connection = null;
		PreparedStatement getJournalVersionPst = null;
		ResultSet rs = null;
		try
		{
			connection = datasource.getConnection();
			getJournalVersionPst = connection
					.prepareStatement("select jid from search_node_status where serverid = ? ");
			getJournalVersionPst.clearParameters();
			getJournalVersionPst.setString(1, serverId);
			rs = getJournalVersionPst.executeQuery();
			if (rs.next())
			{
				version = rs.getLong(1);
			}

		}
		catch (Exception ex)
		{
			log.warn("Unable to get Search Jorunal Version ", ex);
		}
		finally
		{
			try
			{
				rs.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				getJournalVersionPst.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				connection.close();
			}
			catch (Exception ex)
			{
			}

		}
		return version;
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournaledObject#aquireUpdateLock()
	 */
	public boolean aquireUpdateLock()
	{
		try
		{
			boolean locked = rwlock.writeLock().tryLock(10, TimeUnit.SECONDS);
			if (locked)
			{
				setLastJournalEntry(-1);
			}

		}
		catch (InterruptedException iex)
		{

		}
		return false;
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournaledObject#releaseUpdateLock()
	 */
	public void releaseUpdateLock()
	{
		if (rwlock.isWriteLockedByCurrentThread())
		{
			rwlock.writeLock().unlock();
			setLastJournalEntry(-1);
		}
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournaledObject#auquireReadLock()
	 */
	public boolean auquireReadLock()
	{
		try
		{
			return rwlock.readLock().tryLock(10, TimeUnit.SECONDS);
		}
		catch (InterruptedException iex)
		{

		}
		return false;
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournaledObject#releaseReadLock()
	 */
	public void releaseReadLock()
	{
		rwlock.readLock().unlock();
	}

	/**
	 * @param nextJournalEntry
	 */
	public void setLastJournalEntry(long lastJournalEntry)
	{
		lastJournalEntryHolder.set(lastJournalEntry);

	}

	/**
	 * @return
	 */
	public long getLastJournalEntry()
	{
		Long l = lastJournalEntryHolder.get();
		if (l == null)
		{
			return -1;
		}
		return l.longValue();
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournaledIndex#addSegment(java.io.File)
	 */
	public void addSegment(File f)
	{
		segments.add(f);
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournaledIndex#getWorkingSpace()
	 */
	public String getWorkingSpace()
	{
		return workingSpace;
	}

	/**
	 * @see org.sakaiproject.search.index.impl.BaseIndexStorage#getIndexSearcher()
	 */
	@Override
	protected IndexSearcher getIndexSearcher() throws IOException
	{

		long reloadStart = System.currentTimeMillis();

		IndexSearcher indexSearcher = new IndexSearcher(getIndexReader());

		long reloadEnd = System.currentTimeMillis();
		if (diagnostics)
		{
			log.info("Reload Complete " + indexSearcher.getIndexReader().numDocs()
					+ " in " + (reloadEnd - reloadStart));
		}
		return indexSearcher;
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.journal.api.JournaledIndex#getDeletionIndexReader()
	 */
	public IndexReader getDeletionIndexReader() throws IOException
	{
		return getIndexReader();
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#centralIndexExists()
	 */
	public boolean centralIndexExists()
	{
		return true;
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#closeIndexReader(org.apache.lucene.index.IndexReader)
	 */
	public void closeIndexReader(IndexReader indexReader) throws IOException
	{
		indexReader.close();
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#closeIndexSearcher(org.apache.lucene.search.IndexSearcher)
	 */
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
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#closeIndexWriter(org.apache.lucene.index.IndexWriter)
	 */
	public void closeIndexWriter(IndexWriter indexWrite) throws IOException
	{
		if (indexWrite != null)
		{
			indexWrite.close();
		}
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#doPostIndexUpdate()
	 */
	public void doPostIndexUpdate() throws IOException
	{
		lastUpdate = System.currentTimeMillis();
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#doPreIndexUpdate()
	 */
	public void doPreIndexUpdate() throws IOException
	{

	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getIndexReader()
	 */
	public IndexReader getIndexReader() throws IOException
	{
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

			IndexReader.unlock(FSDirectory.getDirectory(searchIndexDirectory, true));
			log.warn("Unlocked Lucene Directory for update, hope this is Ok");
		}

		IndexReader[] indexReaders = new IndexReader[segments.size() + 1];
		indexReaders[0] = IndexReader.open(FSDirectory.getDirectory(f, true));
		int i = 1;
		for (File s : segments)
		{
			indexReaders[i] = IndexReader.open(s);
			i++;
		}

		return new MultiReader(indexReaders);

	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getIndexWriter(boolean)
	 */
	public IndexWriter getIndexWriter(boolean create) throws IOException
	{
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

			IndexReader.unlock(FSDirectory.getDirectory(searchIndexDirectory, true));
			log.warn("Unlocked Lucene Directory for update, hope this is Ok");
		}
		return new IndexWriter(searchIndexDirectory, getAnalyzer(), create);
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getLastUpdate()
	 */
	public long getLastUpdate()
	{
		return lastUpdate;
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getSegmentInfoList()
	 */
	public List getSegmentInfoList()
	{

		List<Object> seginfo = new ArrayList<Object>();
		try
		{
			SizeAction sa = new SizeAction();
			File searchDir = new File(searchIndexDirectory);
			FileUtils.recurse(searchDir, sa);
			seginfo.add(new Object[] { "mainsegment", sa.sizeToString(sa.getSize()),
					sa.dateToString(sa.getLastUpdated()) });
			sa.reset();
			for (File s : segments)
			{
				FileUtils.recurse(s, sa);
				seginfo.add(new Object[] { s.getName(), sa.sizeToString(sa.getSize()),
						sa.dateToString(sa.getLastUpdated()) });
				sa.reset();
			}
			seginfo.add(new Object[] { "Total", sa.sizeToString(sa.getTotalSize()), "" });
		}
		catch (IOException ex)
		{
			seginfo.add("Failed to get Segment Info list " + ex.getClass().getName()
					+ " " + ex.getMessage());

		}
		return seginfo;
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#indexExists()
	 */
	public boolean indexExists()
	{
		File f = new File(searchIndexDirectory);
		return f.exists();
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#isMultipleIndexers()
	 */
	public boolean isMultipleIndexers()
	{
		return true;
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#setRecoverCorruptedIndex(boolean)
	 */
	public void setRecoverCorruptedIndex(boolean recover)
	{
		// TODO Auto-generated method stub

	}

	public class SizeAction implements FileUtils.RecurseAction
	{

		private long size = 0;

		private long totalSize = 0;

		private long lastUpdate = 0;

		/**
		 * @see org.sakaiproject.search.util.FileUtils.RecurseAction#doAfterFile(java.io.File)
		 */
		public void doAfterFile(File f)
		{
		}

		/**
		 * @param lastUpdated
		 * @return
		 */
		public String dateToString(long lastUpdated)
		{
			return new Date(lastUpdated).toString();
		}

		/**
		 * @return
		 */
		public long getLastUpdated()
		{
			return lastUpdate;
		}

		/**
		 * @see org.sakaiproject.search.util.FileUtils.RecurseAction#doBeforeFile(java.io.File)
		 */
		public void doBeforeFile(File f)
		{
		}

		/**
		 * @see org.sakaiproject.search.util.FileUtils.RecurseAction#doFile(java.io.File)
		 */
		public void doFile(File file) throws IOException
		{
			size += file.length();
			lastUpdate = Math.max(lastUpdate, file.lastModified());
			totalSize += file.length();
		}

		/**
		 * @return the size
		 */
		public long getSize()
		{
			return size;
		}

		/**
		 * @return the size
		 */
		public long getTotalSize()
		{
			return totalSize;
		}

		/**
		 * @param size
		 *        the size to set
		 */
		public void reset()
		{
			lastUpdate = 0;
			size = 0;
		}

		public String sizeToString(long tsize)
		{
			if (tsize > 1024 * 1024 * 10)
			{
				return String.valueOf(tsize / (1024 * 1024)) + "MB";
			}
			else if (tsize >= 1024 * 1024)
			{
				return String.valueOf(tsize / (1024 * 1024)) + "."
						+ String.valueOf(tsize / (102 * 1024) + "MB");
			}
			else
			{
				return String.valueOf(tsize / (1024)) + "KB";
			}

		}

	}

}
