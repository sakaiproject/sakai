/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.search.journal.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.search.index.AnalyzerFactory;
import org.sakaiproject.search.journal.api.IndexListener;
import org.sakaiproject.search.journal.api.IndexMonitorListener;
import org.sakaiproject.search.journal.api.IndexStorageProvider;
import org.sakaiproject.search.journal.api.JournalErrorException;
import org.sakaiproject.search.journal.api.JournalManager;
import org.sakaiproject.search.journal.api.JournaledIndex;
import org.sakaiproject.search.journal.api.ThreadBinder;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.util.FileUtils;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * <pre>
 *                 This is a Journaled savePoint of the local FSIndexStorage. It will merge in new
 *                 savePoints from the jorunal. This is going to be performed in a non
 *                 transactional way for the moment. 
 *                 
 *                 The index reader must maintain a single
 *                 index reader for the JVM. When performing a read update, the single index
 *                 reader must be used, but each time the index reader is provided we should
 *                 check that the index reader has not been updated.
 *                 
 *                 If the reader is being updated, then it is not safe to reload it.
 * </pre>
 * 
 * @author ieb
 */
public class JournaledFSIndexStorage implements JournaledIndex, IndexStorageProvider
{

	private static final Log log = LogFactory.getLog(JournaledFSIndexStorage.class);

	private static final String SEGMENT_LIST_NAME = "local-segments";

	/**
	 * Sakai config service
	 */
	private ServerConfigurationService serverConfigurationService;

	/**
	 * The server Id
	 */
	private String serverId;

	/**
	 * The datasource IoC injected
	 */
	private DataSource datasource;

	/**
	 * A lock to protect the local index
	 */
	private ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock(true);

	/**
	 * A holder on the thread used to store the journal entry that is being
	 * processed by the thread
	 */
	private ThreadLocal<Long> lastJournalEntryHolder = new ThreadLocal<Long>();

	/**
	 * The journal manager that manages the journal that feeds this index
	 */
	private JournalManager journalManager;

	/**
	 * A list of temporary segments
	 */
	private List<File> segments = new ArrayList<File>();

	/**
	 * the timestamp when the index was updated
	 */
	private long lastUpdate;

	/**
	 * The number of the journal that was last applied to this index
	 */
	private long journalSavePoint = -1;

	/**
	 * The current reader used for deletes and searching, this is a singleton,
	 * only reloaded when changes in the index are detected. A read lock is
	 * required to allow reloading of the index
	 */
	private MultiReader multiReader;

	/**
	 * Listeners of this index
	 */
	private List<IndexListener> indexListeners = new ArrayList<IndexListener>();

	/**
	 * Have the segments been modified
	 */
	private boolean modified = false;

	/**
	 * A permanent indexWriter. To get the permanent Index Writer a write lock
	 * must be taken on the index. This will not prevent access to the
	 * multReader, but it will prevent it from being reloaded
	 */
	private IndexWriter permanentIndexWriter;

	private AnalyzerFactory analyzerFactory;

	private IndexSearcher indexSearcher = null;

	private ClusterService clusterService;

	private long lastLoad;

	private long lastLoadTime;

	private JournalSettings journalSettings;

	private ThreadLocalManager threadLocalManager;

	public void destroy()
	{

	}

	/**
	 * @see org.sakaiproject.search.index.impl.FSIndexStorage#init()
	 */
	public void init()
	{
		serverId = serverConfigurationService.getServerId();
		File f = new File(journalSettings.getSearchIndexDirectory(), SEGMENT_LIST_NAME);
		if (f.exists())
		{
			log.info("Segment List File Exists, using it");
			try
			{
				loadSegmentList();
			}
			catch (IOException e)
			{
				log.error("Unable to load segment list", e);
				System.exit(-10);
			}
		}
		else
		{
			log.info("No Segment List File Exists");

			// If no segments list exists, we have to assume that this a clean
			// node.
			// we must, clean the index if it exists and delete the db record
			// that records
			// where this node is.

			File searchDirectory = new File(journalSettings.getSearchIndexDirectory());
			if (searchDirectory.exists())
			{
				log
						.warn("Found Existing Search Directory with no local segment list, I will delete "
								+ searchDirectory.getAbsolutePath());
				try
				{
					FileUtils.deleteAll(searchDirectory);
				}
				catch (IOException e)
				{
					log.warn("Unable to remove Existing Search Directory ", e);
				}
			}
			deleteJournalSavePoint();

		}
		// ensure that the index is closed to avoid stale locks
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			/*
			 * *
			 * 
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run()
			{
				try
				{
					if (multiReader != null)
						multiReader.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);
				}
			}
		});
	}

	/**
	 * 
	 */
	private void deleteJournalSavePoint()
	{
		Connection connection = null;
		PreparedStatement deleteJournalSavePointPst = null;
		if (datasource != null)
		{
			try
			{
				connection = datasource.getConnection();
				boolean tableExists = false;
				PreparedStatement checkJournalSavePoint = null;
				ResultSet rs = null;
				try
				{
					checkJournalSavePoint = connection
							.prepareStatement("select count(*) from search_node_status ");
					rs = checkJournalSavePoint.executeQuery();
					if (rs.next())
					{
						tableExists = true;
					}
				}
				catch (Exception ex)
				{
					if (log.isDebugEnabled())
					{
						log
								.debug(
										"Failed to check for existance of table, this is Ok on first start ",
										ex);
					}
				}
				finally
				{
					try
					{
						rs.close();
					}
					catch (Exception ex)
					{
						log.debug(ex);

					}
					try
					{
						checkJournalSavePoint.close();
					}
					catch (Exception ex)
					{
						log.debug(ex);
					}
				}
				if (tableExists)
				{
					deleteJournalSavePointPst = connection
							.prepareStatement("delete from search_node_status where serverid = ? ");
					deleteJournalSavePointPst.clearParameters();
					deleteJournalSavePointPst.setString(1, serverId);
					deleteJournalSavePointPst.executeUpdate();
					connection.commit();
				}
			}
			catch (Exception ex)
			{
				log.warn("Unable to delete Search Jorunal SavePoint ", ex);
			}
			finally
			{
				try
				{
					deleteJournalSavePointPst.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);
				}
				try
				{
					connection.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);
				}
			}
		}
	}

	/**
	 * Since this is a singleton, we can cache the savePoint only updating on
	 * change. The Save Point is the number of the journal entry that this node
	 * has merged upto and including
	 * 
	 * @see org.sakaiproject.search.maintanence.impl.JournaledObject#getJounalSavePoint()
	 */
	public long getJournalSavePoint()
	{
		if (journalSavePoint == -1)
		{
			Connection connection = null;
			PreparedStatement getJournalSavePointPst = null;
			ResultSet rs = null;
			try
			{
				connection = datasource.getConnection();
				getJournalSavePointPst = connection
						.prepareStatement("select jid from search_node_status where serverid = ? ");
				getJournalSavePointPst.clearParameters();
				getJournalSavePointPst.setString(1, serverId);
				rs = getJournalSavePointPst.executeQuery();
				if (rs.next())
				{
					journalSavePoint = rs.getLong(1);
					lastUpdate = System.currentTimeMillis();
				}

			}
			catch (Exception ex)
			{
				log.warn("Unable to get Search Jorunal SavePoint ", ex);
			}
			finally
			{
				try
				{
					rs.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);
				}
				try
				{
					getJournalSavePointPst.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);
				}
				try
				{
					connection.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);
				}

			}
		}
		return journalSavePoint;
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
			log.debug(iex);
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
			log.debug(iex);

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
		modified = true;
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournaledIndex#getWorkingSpace()
	 */
	public String getWorkingSpace()
	{
		return journalSettings.getLocalIndexWorkingSpace();
	}

	/**
	 * @see org.sakaiproject.search.index.impl.BaseIndexStorage#getIndexSearcher()
	 */
	public IndexSearcher getIndexSearcher() throws IOException
	{

		log.debug("getIndexSearcher(): " + indexSearcher);

		if (indexSearcher == null)
		{
			loadIndexSearcherInternal();
		}
		if (indexSearcher instanceof ThreadBinder)
		{
			log.debug("getIndexSearcher(): " + indexSearcher + " threadbinder");
			((ThreadBinder) indexSearcher).bind(threadLocalManager);
		}

		return indexSearcher;
	}

	/**
	 * @throws IOException
	 */
	private void loadIndexSearcherInternal() throws IOException
	{
		log.debug("loadIndexSearcherInternal()");

		IndexReader tmpIndexReader = multiReader;
		final IndexReader ir = getIndexReader();

		log.debug("index reader is: " + ir);

		if (tmpIndexReader != ir || indexSearcher == null)
		{
			long start = System.currentTimeMillis();

			RefCountIndexSearcher newIndexSearcher = new RefCountIndexSearcher(ir, this);
			if (indexSearcher != null)
			{
				indexSearcher.close(); // this will be postponed
			}
			indexSearcher = newIndexSearcher;
			long end = System.currentTimeMillis();
			lastLoad = end;
			lastLoadTime = end - start;
			log.debug("Opened index Searcher in " + (end - start) + " ms");
			fireIndexSearcherOpen(indexSearcher);
		}
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
		throw new UnsupportedOperationException(
				"Only Readers are available from a JournaledFSIndexStorage class");
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#doPostIndexUpdate()
	 */
	public void doPostIndexUpdate() throws IOException
	{
		throw new UnsupportedOperationException(
				"Only Readers are available from a JournaledFSIndexStorage class");
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#doPreIndexUpdate()
	 */
	public void doPreIndexUpdate() throws IOException
	{
		throw new UnsupportedOperationException(
				"Only Readers are available from a JournaledFSIndexStorage class");
	}

	public IndexReader getIndexReader() throws IOException
	{
		log.debug("getIndexReader()");

		boolean current = false;
		long start = System.currentTimeMillis();
		try
		{
			// could be null
			if (multiReader != null)
			{
				current = multiReader.isCurrent();
			}
		}
		catch (Exception ex)
		{
			log.warn("Failed to get current status assuming index reload is required ",
					ex);
		}
		long start2 = System.currentTimeMillis();
		long x1 = start2;
		long f1 = start2;
		long r1 = start2;
		long r2 = start2;
		long f2 = start2;
		long f3 = start2;
		long x2 = start2;

		long tlock = 0;
		if (modified || multiReader == null || !current)
		{
			modified = false;
			/*
			 * We must get a read lock to prevent a writer from opening when we
			 * are trying to open for read. Writers will have already taken
			 * write locks and so get the read lock by default.
			 */

			x1 = System.currentTimeMillis();
			f1 = x1;
			boolean locked = false;
			if (rwlock.isWriteLockedByCurrentThread())
			{
				locked = true;
			}
			else
			{
				locked = aquireReadLock();
			}
			if (locked)
			{
				// should check again
				current = false;
				try
				{
					// could be null
					if (multiReader != null)
					{
						current = multiReader.isCurrent();
					}
				}
				catch (Exception ex)
				{
					log
							.warn(
									"Failed to get current status assuming index reload is required ",
									ex);
				}
				if (modified || multiReader == null || !current)
				{
					f1 = System.currentTimeMillis();
					try
					{
						r1 = System.currentTimeMillis();
						getIndexReaderInternal();
						r2 = System.currentTimeMillis();
					}
					finally
					{
						f2 = System.currentTimeMillis();
						if (!rwlock.isWriteLockedByCurrentThread())
						{
							releaseReadLock();
						}
						f3 = System.currentTimeMillis();
					}
				}
			}
			else
			{
				f1 = System.currentTimeMillis();
				log.warn("Failed to get read lock on index ");
			}
			x2 = System.currentTimeMillis();
		}
		long f = System.currentTimeMillis();
		if ((f - start) > 1000)
		{
			log
					.info("Long time opening " + (start2 - start) + ":" + (f - start2)
							+ " ms");
			log.info("Read Lock aquire " + (f1 - x1) + " ms");
			log.info("Index Load aquire " + (r2 - r1) + " ms");
			log.info("Read Lock Release " + (f3 - f3) + " ms");
		}
		if (multiReader instanceof ThreadBinder)
		{
			((ThreadBinder) multiReader).bind(threadLocalManager);
		}
		
		log.debug("getIndexReader() returning " + multiReader);
		return multiReader; 
	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getIndexReader()
	 */
	private IndexReader getIndexReaderInternal() throws IOException
	{
		long start = System.currentTimeMillis();
		File f = new File(journalSettings.getSearchIndexDirectory());
		Directory d = null;
		if (!f.exists())
		{
			f.mkdirs();
			log.debug("Indexing in " + f.getAbsolutePath());
			d = FSDirectory.getDirectory(journalSettings.getSearchIndexDirectory(), true);
		}
		else
		{
			d = FSDirectory
					.getDirectory(journalSettings.getSearchIndexDirectory(), false);
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
		if (!d.fileExists("segments.gen"))
		{
			IndexWriter iw = new IndexWriter(f, getAnalyzer(), true);
			iw.setUseCompoundFile(true);
			iw.setMaxMergeDocs(journalSettings.getLocalMaxMergeDocs());
			iw.setMaxBufferedDocs(journalSettings.getLocalMaxBufferedDocs());
			iw.setMergeFactor(journalSettings.getLocalMaxMergeFactor());
			Document doc = new Document();
			doc.add(new Field("indexcreated", (new Date()).toString(), Field.Store.YES,
					Field.Index.UN_TOKENIZED, Field.TermVector.NO));
			iw.addDocument(doc);
			iw.close();
		}

		final IndexReader[] indexReaders = new IndexReader[segments.size() + 1];
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
		RefCountMultiReader newMultiReader = new RefCountMultiReader(indexReaders, this);

		if (multiReader != null)
		{
			multiReader.close(); // this will postpone due to the override
			// above
		}
		multiReader = newMultiReader;
		lastUpdate = System.currentTimeMillis();

		log.debug("Reopened Index Reader in " + (lastUpdate - start) + " ms");
		// notify anything that wants to listen to the index open and close
		// events
		fireIndexReaderOpen(newMultiReader);
		return multiReader;

	}

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#getIndexWriter(boolean)
	 */
	public IndexWriter getIndexWriter(boolean create) throws IOException
	{
		throw new UnsupportedOperationException(
				"A JournaledFSIndexStorage only supports readers");

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
			File searchDir = new File(journalSettings.getSearchIndexDirectory());
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
		File f = new File(journalSettings.getSearchIndexDirectory());
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

	/*
	 * *
	 * 
	 * @see org.sakaiproject.search.journal.api.JournaledIndex#setJournalIndexEntry(long)
	 */
	public void setJournalIndexEntry(long journalEntry)
	{
		if (journalSavePoint != journalEntry)
		{
			Connection connection = null;
			PreparedStatement updateJournalSavePointPst = null;
			PreparedStatement insertJournalSavePointPst = null;
			try
			{
				connection = datasource.getConnection();
				updateJournalSavePointPst = connection
						.prepareStatement("update search_node_status set jid = ?, jidts = ? where  serverid = ? ");
				updateJournalSavePointPst.clearParameters();
				updateJournalSavePointPst.setLong(1, journalEntry);
				updateJournalSavePointPst.setLong(2, System.currentTimeMillis());
				updateJournalSavePointPst.setString(3, serverId);
				if (updateJournalSavePointPst.executeUpdate() != 1)
				{
					insertJournalSavePointPst = connection
							.prepareStatement("insert into  search_node_status (jid,jidts,serverid) values (?,?,?) ");
					insertJournalSavePointPst.clearParameters();
					insertJournalSavePointPst.setLong(1, journalEntry);
					insertJournalSavePointPst.setLong(2, System.currentTimeMillis());
					insertJournalSavePointPst.setString(3, serverId);
					if (insertJournalSavePointPst.executeUpdate() != 1)
					{
						throw new SQLException(
								"Unable to update journal entry for some reason ");
					}
				}
				connection.commit();
				journalSavePoint = journalEntry;

			}
			catch (Exception ex)
			{
				try
				{
					connection.rollback();
				}
				catch (Exception ex2)
				{
					log.debug(ex);
				}
				log.warn("Unable to get Search Jorunal SavePoint ", ex);
			}
			finally
			{
				try
				{
					if (updateJournalSavePointPst != null)
						updateJournalSavePointPst.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);
				}
				try
				{
					if (insertJournalSavePointPst != null)
						insertJournalSavePointPst.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);
				}
				try
				{
					if (connection != null)
						connection.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);
				}

			}
		}
	}

	/**
	 * @param oldMultiReader
	 * @throws IOException
	 */
	protected void fireIndexReaderClose(IndexReader oldMultiReader) throws IOException
	{
		if (oldMultiReader == multiReader)
		{
			multiReader = null;
		}
		for (Iterator<IndexListener> itl = getIndexListeners().iterator(); itl.hasNext();)
		{
			IndexListener tl = itl.next();
			tl.doIndexReaderClose(oldMultiReader);
		}
	}

	/**
	 * @param newMultiReader
	 */
	protected void fireIndexReaderOpen(IndexReader newMultiReader)
	{
		for (Iterator<IndexListener> itl = getIndexListeners().iterator(); itl.hasNext();)
		{
			IndexListener tl = itl.next();
			tl.doIndexReaderOpen(newMultiReader);
		}
	}

	/**
	 * @param indexSearcher2
	 * @throws IOException
	 */
	void fireIndexSearcherClose(IndexSearcher indexSearcher) throws IOException
	{
		for (Iterator<IndexListener> itl = getIndexListeners().iterator(); itl.hasNext();)
		{
			IndexListener tl = itl.next();
			tl.doIndexSearcherClose(indexSearcher);
		}

	}

	/**
	 * @param indexSearcher2
	 * @throws IOException
	 */
	private void fireIndexSearcherOpen(IndexSearcher indexSearcher) throws IOException
	{
		for (Iterator<IndexListener> itl = getIndexListeners().iterator(); itl.hasNext();)
		{
			IndexListener tl = itl.next();
			tl.doIndexSearcherOpen(indexSearcher);
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
		tl.addAll(this.indexListeners);
		tl.add(indexListener);
		this.indexListeners = tl;
	}

	public void setIndexListener(List<IndexListener> indexListeners)
	{
		List<IndexListener> tl = new ArrayList<IndexListener>();
		tl.addAll(indexListeners);
		this.indexListeners = tl;
	}

	/**
	 * Get an index writer to the permanent index, a write lock should have been
	 * taken before doing this
	 * 
	 * @throws IndexTransactionException
	 * @see org.sakaiproject.search.journal.api.JournaledIndex#getPermanentIndexWriter()
	 */
	public IndexWriter getPermanentIndexWriter() throws IndexTransactionException
	{
		if (!rwlock.isWriteLockedByCurrentThread())
		{
			throw new JournalErrorException(
					"Thread Does not hold a write lock, permanent index cannot be written to ");
		}
		if (permanentIndexWriter == null)
		{
			try
			{
				File f = new File(journalSettings.getSearchIndexDirectory());
				Directory d = null;
				if (!f.exists())
				{
					f.mkdirs();
					log.debug("Indexing in " + f.getAbsolutePath());
					d = FSDirectory.getDirectory(journalSettings
							.getSearchIndexDirectory(), true);
				}
				else
				{
					d = FSDirectory.getDirectory(journalSettings
							.getSearchIndexDirectory(), false);
				}

				if (!d.fileExists("segments.gen"))
				{
					permanentIndexWriter = new IndexWriter(f, getAnalyzer(), true);
					permanentIndexWriter.setUseCompoundFile(true);
					permanentIndexWriter.setMaxMergeDocs(journalSettings.getLocalMaxMergeDocs());
					permanentIndexWriter.setMaxBufferedDocs(journalSettings.getLocalMaxBufferedDocs());
					permanentIndexWriter.setMergeFactor(journalSettings.getLocalMaxMergeFactor());
					Document doc = new Document();
					doc.add(new Field("indexcreated", (new Date()).toString(),
							Field.Store.YES, Field.Index.UN_TOKENIZED,
							Field.TermVector.NO));
					permanentIndexWriter.addDocument(doc);
					permanentIndexWriter.close();
				}
				permanentIndexWriter = new MonitoredIndexWriter(f, getAnalyzer(), false);
				((MonitoredIndexWriter) permanentIndexWriter)
						.addMonitorIndexListener(new IndexMonitorListener()
						{

							public void doIndexMonitorClose(IndexWriter writer)
							{
								permanentIndexWriter = null;
							}

						});
				permanentIndexWriter.setUseCompoundFile(true);
				permanentIndexWriter.setMaxMergeDocs(100000);
				permanentIndexWriter.setMaxBufferedDocs(50);
				permanentIndexWriter.setMergeFactor(50);
			}
			catch (IOException ioex)
			{
				throw new JournalErrorException("Failed to open permanent Index ", ioex);
			}
		}
		return permanentIndexWriter;
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournaledIndex#getSegments()
	 */
	public File[] getSegments()
	{
		return segments.toArray(new File[0]);
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournaledIndex#setSegments(java.util.List)
	 */
	public void setSegments(List<File> keep)
	{
		segments = keep;
	}

	public Analyzer getAnalyzer()
	{
		return analyzerFactory.newAnalyzer();
	}

	/**
	 * @return Returns the analzyserFactory.
	 */
	public AnalyzerFactory getAnalyzerFactory()
	{
		return analyzerFactory;
	}

	/**
	 * @param analzyserFactory
	 *        The analzyserFactory to set.
	 */
	public void setAnalyzerFactory(AnalyzerFactory analzyserFactory)
	{
		this.analyzerFactory = analzyserFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.JournaledIndex#saveSegmentList()
	 */
	public void saveSegmentList() throws IOException
	{
		File f = new File(journalSettings.getSearchIndexDirectory(), SEGMENT_LIST_NAME);
		SegmentListWriter sw = new SegmentListWriter(f);
		sw.write(segments);
	}

	public void loadSegmentList() throws IOException
	{
		File f = new File(journalSettings.getSearchIndexDirectory(), SEGMENT_LIST_NAME);
		SegmentListReader sr = new SegmentListReader(f);
		segments = sr.read();
	}

	/**
	 * @return the clusterService
	 */
	public ClusterService getClusterService()
	{
		return clusterService;
	}

	/**
	 * @param clusterService
	 *        the clusterService to set
	 */
	public void setClusterService(ClusterService clusterService)
	{
		this.clusterService = clusterService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.JournaledIndex#loadIndexReader()
	 */
	public void loadIndexReader() throws IOException
	{
		loadIndexSearcherInternal();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.JournaledObject#debugLock()
	 */
	public void debugLock()
	{
		log.info(this + " Locks Waiting " + rwlock.getQueueLength());
		log.info(this + " Read Locks Locks " + rwlock.getReadLockCount());
		log.info(this + " Write Holds this thread " + rwlock.getWriteHoldCount());
		log.info(this + " is Write Locked " + rwlock.isWriteLocked());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.IndexStorageProvider#getLastLoad()
	 */
	public long getLastLoad()
	{
		return lastLoad;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.IndexStorageProvider#getLastLoadTime()
	 */
	public long getLastLoadTime()
	{
		return lastLoadTime;
	}

	/**
	 * @return the journalSettings
	 */
	public JournalSettings getJournalSettings()
	{
		return journalSettings;
	}

	/**
	 * @param journalSettings
	 *        the journalSettings to set
	 */
	public void setJournalSettings(JournalSettings journalSettings)
	{
		this.journalSettings = journalSettings;
	}

	public void markModified() throws IOException
	{
		modified = true;
		if (multiReader != null)
		{
			multiReader.close();
			multiReader = null;
		}
	}

	/**
	 * @return the threadLocalManager
	 */
	public ThreadLocalManager getThreadLocalManager()
	{
		return threadLocalManager;
	}

	/**
	 * @param threadLocalManager
	 *        the threadLocalManager to set
	 */
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager)
	{
		this.threadLocalManager = threadLocalManager;
	}

}
