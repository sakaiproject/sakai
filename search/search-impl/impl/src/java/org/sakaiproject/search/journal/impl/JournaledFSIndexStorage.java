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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.search.index.impl.BaseIndexStorage;
import org.sakaiproject.search.journal.api.IndexListener;
import org.sakaiproject.search.journal.api.JournalManager;
import org.sakaiproject.search.journal.api.JournaledIndex;
import org.sakaiproject.search.util.FileUtils;

/**
 * <pre>
 * This is a Journaled version of the local FSIndexStorage. It will merge in new
 * versions from the jorunal. This is going to be performed in a non
 * transactional way for the moment. 
 * 
 * The index reader must maintain a single
 * index reader for the JVM. When performing a read update, the single index
 * reader must be used, but each time the index reader is provided we should
 * check that the index reader has not been updated.
 * 
 * If the reader is being updated, then it is not safe to reload it.
 * </pre>
 * @author ieb TODO Unit test
 */
public class JournaledFSIndexStorage extends BaseIndexStorage implements JournaledIndex
{

	private static final Log log = LogFactory.getLog(JournaledFSIndexStorage.class);

	private ServerConfigurationService serverConfigurationService;

	private String serverId;

	private DataSource datasource;

	private ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock(true);

	private ThreadLocal<Long> lastJournalEntryHolder = new ThreadLocal<Long>();

	private JournalManager journalManager;

	private String workingSpace;

	private String searchIndexDirectory;

	private List<File> segments = new ArrayList<File>();

	private long lastUpdate;

	private long journalVersion = -1;

	private MultiReader multiReader;

	private List<IndexListener> indexListeners = new ArrayList<IndexListener>();

	private boolean modified = false;

	/**
	 * @see org.sakaiproject.search.index.impl.FSIndexStorage#init()
	 */
	public void init()
	{
		serverId = serverConfigurationService.getServerId();
		// ensure that the index is closed to avoid stale locks
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run()
			{
				try
				{
					multiReader.close();
				}
				catch (Exception ex)
				{
				}
			}
		});
	}

	/**
	 * Since this is a singleton, we can cahce the version only updating on
	 * change
	 * 
	 * @see org.sakaiproject.search.maintanence.impl.JournaledObject#getJounalVersion()
	 */
	public long getJournalVersion()
	{
		if (journalVersion == -1)
		{
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
					journalVersion = rs.getLong(1);
					lastUpdate = System.currentTimeMillis();
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
		}
		return journalVersion;
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
			return locked;

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
	public boolean aquireReadLock()
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
		modified  = true;
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
		throw new UnsupportedOperationException("Only Readers are available from a JournaledFSIndexStorage class");
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#doPostIndexUpdate()
	 */
	public void doPostIndexUpdate() throws IOException
	{
		throw new UnsupportedOperationException("Only Readers are available from a JournaledFSIndexStorage class");
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#doPreIndexUpdate()
	 */
	public void doPreIndexUpdate() throws IOException
	{
		throw new UnsupportedOperationException("Only Readers are available from a JournaledFSIndexStorage class");
	}

	public IndexReader getIndexReader() throws IOException
	{
		if (modified || multiReader == null || !multiReader.isCurrent())
		{
			/*
			 * We must get a read lock to prevent a writer from 
			 * opening when we are trying to open for read.
			 * Writers will have already taken write locks
			 * and so get the read lock by default. 
			 */
			if ( aquireReadLock() ) {
				try {
					getIndexReaderInternal();
				} finally {
					releaseReadLock();
				}
			}
		}
		return multiReader;
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getIndexReader()
	 */
	private IndexReader getIndexReaderInternal() throws IOException
	{
		File f = new File(searchIndexDirectory);
		Directory d = null;
		if (!f.exists())
		{
			f.mkdirs();
			log.debug("Indexing in " + f.getAbsolutePath());
			d = FSDirectory.getDirectory(searchIndexDirectory, true);
		}
		else
		{
			d = FSDirectory.getDirectory(searchIndexDirectory, false);
		}

		if (IndexReader.isLocked(d))
		{
			// this could be dangerous, I am assuming that
			// the locking mechanism implemented here is
			// robust and
			// already prevents multiple modifiers.
			// A more

			IndexReader.unlock(d);
			log.warn("Unlocked Lucene Directory for update, hope this is Ok");
		}
		if (!d.fileExists("segments"))
		{
			IndexWriter iw = new IndexWriter(f, getAnalyzer(), true);
			iw.setUseCompoundFile(true);
			iw.setMaxBufferedDocs(50);
			iw.setMaxMergeDocs(50);
			Document doc = new Document();
			doc.add(new Field("indexcreated", (new Date()).toString(), Field.Store.YES,
					Field.Index.UN_TOKENIZED, Field.TermVector.NO));
			iw.addDocument(doc);
			iw.close();
		}

		IndexReader[] indexReaders = new IndexReader[segments.size() + 1];
		indexReaders[0] = IndexReader.open(d);
		int i = 1;
		for (File s : segments)
		{
			FSDirectory fsd = FSDirectory.getDirectory(s, false);
			if (IndexReader.isLocked(fsd))
			{
				log.warn("++++++++++++++++++Unlocking Index " + fsd.toString());
				IndexReader.unlock(fsd);
			}
			indexReaders[i] = IndexReader.open(s);
			i++;
		}
		MultiReader newMultiReader = new MultiReader(indexReaders);
		MultiReader oldMultiReader = multiReader;
		multiReader = newMultiReader;
		lastUpdate = System.currentTimeMillis();

		// notify anything that wants to listen to the index open and close
		// events
		fireIndexReaderOpen(newMultiReader);
		fireIndexReaderClose(oldMultiReader);

		oldMultiReader = null;
		return multiReader;

	}


	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getIndexWriter(boolean)
	 */
	public IndexWriter getIndexWriter(boolean create) throws IOException
	{
		throw new UnsupportedOperationException("A JournaledFSIndexStorage only supports readers");

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

	/**
	 * @see org.sakaiproject.search.index.IndexStorage#setRecoverCorruptedIndex(boolean)
	 */
	public void setRecoverCorruptedIndex(boolean recover)
	{
		log.warn("Recover Indexes not implemented, yet");
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

	/**
	 * @return the datasource
	 */
	public DataSource getDatasource()
	{
		return datasource;
	}

	/**
	 * @param datasource
	 *        the datasource to set
	 */
	public void setDatasource(DataSource datasource)
	{
		this.datasource = datasource;
	}

	/**
	 * @return the journalMonitor
	 */
	public JournalManager getJournalManager()
	{
		return journalManager;
	}

	/**
	 * @param journalMonitor
	 *        the journalMonitor to set
	 */
	public void setJournalManager(JournalManager journalManager)
	{
		this.journalManager = journalManager;
	}

	/**
	 * @return the searchIndexDirectory
	 */
	public String getSearchIndexDirectory()
	{
		return searchIndexDirectory;
	}

	/**
	 * @param searchIndexDirectory
	 *        the searchIndexDirectory to set
	 */
	public void setSearchIndexDirectory(String searchIndexDirectory)
	{
		this.searchIndexDirectory = searchIndexDirectory;
	}

	/**
	 * @return the serverConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService()
	{
		return serverConfigurationService;
	}

	/**
	 * @param serverConfigurationService
	 *        the serverConfigurationService to set
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	/**
	 * @param workingSpace
	 *        the workingSpace to set
	 */
	public void setWorkingSpace(String workingSpace)
	{
		this.workingSpace = workingSpace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.JournaledIndex#setJournalIndexEntry(long)
	 */
	public void setJournalIndexEntry(long journalEntry)
	{
		if (journalVersion != journalEntry)
		{
			Connection connection = null;
			PreparedStatement updateJournalVersionPst = null;
			PreparedStatement insertJournalVersionPst = null;
			try
			{
				connection = datasource.getConnection();
				updateJournalVersionPst = connection
						.prepareStatement("update search_node_status set jid = ?, jidts = ? where  serverid = ? ");
				updateJournalVersionPst.clearParameters();
				updateJournalVersionPst.setLong(1, journalEntry);
				updateJournalVersionPst.setLong(2, System.currentTimeMillis());
				updateJournalVersionPst.setString(3, serverId);
				if (updateJournalVersionPst.executeUpdate() != 1)
				{
					insertJournalVersionPst = connection
							.prepareStatement("insert into  search_node_status (jid,jidts,serverid) values (?,?,?) ");
					insertJournalVersionPst.clearParameters();
					insertJournalVersionPst.setLong(1, journalEntry);
					insertJournalVersionPst.setLong(2, System.currentTimeMillis());
					insertJournalVersionPst.setString(3, serverId);
					if (insertJournalVersionPst.executeUpdate() != 1)
					{
						throw new SQLException(
								"Unable to update journal entry for some reason ");
					}
				}
				connection.commit();
				journalVersion = journalEntry;

			}
			catch (Exception ex)
			{
				try
				{
					connection.rollback();
				}
				catch (Exception ex2)
				{
				}
				log.warn("Unable to get Search Jorunal Version ", ex);
			}
			finally
			{
				try
				{
					updateJournalVersionPst.close();
				}
				catch (Exception ex)
				{
				}
				try
				{
					insertJournalVersionPst.close();
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
		}
	}

	/**
	 * @param oldMultiReader
	 */
	private void fireIndexReaderClose(IndexReader oldMultiReader)
	{
		for (Iterator<IndexListener> itl = getIndexListeners()
				.iterator(); itl.hasNext();)
		{
			IndexListener tl = itl.next();
			tl.doIndexReaderClose(oldMultiReader);
		}
	}

	/**
	 * @param newMultiReader
	 */
	private void fireIndexReaderOpen(IndexReader newMultiReader)
	{
		for (Iterator<IndexListener> itl = getIndexListeners()
				.iterator(); itl.hasNext();)
		{
			IndexListener tl = itl.next();
			tl.doIndexReaderOpen(newMultiReader);
		}
	}

	
	/**
	 * @return
	 */
	private List<IndexListener> getIndexListeners()
	{
		return indexListeners;
	}

	public void addIndexListener(IndexListener indexListener)
	{
		List<IndexListener> tl = new ArrayList<IndexListener>();
		tl.addAll(this.indexListeners );
		tl.add(indexListener);
		this.indexListeners = tl;
	}
	public void setIndexListener(List<IndexListener> indexListeners)
	{
		List<IndexListener> tl = new ArrayList<IndexListener>();
		tl.addAll(this.indexListeners);
		this.indexListeners = tl;
	}


}
