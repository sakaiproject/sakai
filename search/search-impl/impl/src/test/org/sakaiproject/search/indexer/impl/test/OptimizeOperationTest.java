/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.search.indexer.impl.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.search.index.impl.StandardAnalyzerFactory;
import org.sakaiproject.search.indexer.impl.JournalManagerUpdateTransaction;
import org.sakaiproject.search.indexer.impl.JournalStorageUpdateTransactionListener;
import org.sakaiproject.search.indexer.impl.SearchBuilderQueueManager;
import org.sakaiproject.search.indexer.impl.TransactionIndexManagerImpl;
import org.sakaiproject.search.indexer.impl.TransactionalIndexWorker;
import org.sakaiproject.search.journal.api.IndexListener;
import org.sakaiproject.search.journal.impl.DbJournalManager;
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorage;
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorageUpdateTransactionListener;
import org.sakaiproject.search.journal.impl.MergeUpdateManager;
import org.sakaiproject.search.journal.impl.MergeUpdateOperation;
import org.sakaiproject.search.journal.impl.SharedFilesystemJournalStorage;
import org.sakaiproject.search.mock.MockSearchIndexBuilder;
import org.sakaiproject.search.mock.MockServerConfigurationService;
import org.sakaiproject.search.optimize.impl.OptimizableIndexImpl;
import org.sakaiproject.search.optimize.impl.OptimizeIndexManager;
import org.sakaiproject.search.optimize.impl.OptimizeIndexOperation;
import org.sakaiproject.search.optimize.impl.OptimizeTransactionListenerImpl;
import org.sakaiproject.search.transaction.impl.LocalTransactionSequenceImpl;
import org.sakaiproject.search.transaction.impl.TransactionSequenceImpl;
import org.sakaiproject.search.util.FileUtils;

/**
 * @author ieb
 */
public class OptimizeOperationTest extends TestCase
{

	private static final Log log = LogFactory.getLog(OptimizeOperationTest.class);

	private File testBase;

	private File work;

	private File shared;

	private File index;

	private TDataSource tds;

	private MergeUpdateOperation mu;

	private TransactionalIndexWorker tiw;

	private OptimizeIndexOperation oo;

	private File optimizeWork;

	private OptimizableIndexImpl optimizableIndex;

	private JournaledFSIndexStorage journaledFSIndexStorage;

	protected ThreadLocal<Object> inclose = new ThreadLocal<Object>();

	protected ThreadLocal<Object> insearcherclose = new ThreadLocal<Object>();

	/**
	 * @param name
	 */
	public OptimizeOperationTest(String name)
	{
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
		testBase = new File("m2-target");
		testBase = new File(testBase, "OptimizeOperationTest");
		work = new File(testBase, "work");
		optimizeWork = new File(testBase, "optwork");
		shared = new File(testBase, "shared");
		index = new File(testBase, "index");

		tds = new TDataSource(5, false);

		mu = new MergeUpdateOperation();
		journaledFSIndexStorage = new JournaledFSIndexStorage();
		StandardAnalyzerFactory analyzerFactory = new StandardAnalyzerFactory();
		DbJournalManager journalManager = new DbJournalManager();
		MockServerConfigurationService serverConfigurationService = new MockServerConfigurationService();
		MergeUpdateManager mergeUpdateManager = new MergeUpdateManager();
		TransactionSequenceImpl sequence = new TransactionSequenceImpl();

		SharedFilesystemJournalStorage sharedFilesystemJournalStorage = new SharedFilesystemJournalStorage();
		JournaledFSIndexStorageUpdateTransactionListener journaledFSIndexStorageUpdateTransactionListener = new JournaledFSIndexStorageUpdateTransactionListener();

		sequence.setDatasource(tds.getDataSource());
		sequence.setName("TransactionalIndexWorkerTest");

		journaledFSIndexStorageUpdateTransactionListener
				.setJournaledIndex(journaledFSIndexStorage);
		journaledFSIndexStorageUpdateTransactionListener
				.setJournalManager(journalManager);
		journaledFSIndexStorageUpdateTransactionListener
				.setJournalStorage(sharedFilesystemJournalStorage);

		sharedFilesystemJournalStorage.setJournalLocation(shared.getAbsolutePath());

		sequence.setDatasource(tds.getDataSource());

		mergeUpdateManager
				.addTransactionListener(journaledFSIndexStorageUpdateTransactionListener);

		mergeUpdateManager.setSequence(sequence);

		journalManager.setDatasource(tds.getDataSource());

		journaledFSIndexStorage.setAnalyzerFactory(analyzerFactory);
		journaledFSIndexStorage.setDatasource(tds.getDataSource());
		journaledFSIndexStorage.setJournalManager(journalManager);
		journaledFSIndexStorage.setRecoverCorruptedIndex(false);
		journaledFSIndexStorage.setSearchIndexDirectory(index.getAbsolutePath());
		journaledFSIndexStorage.setServerConfigurationService(serverConfigurationService);
		journaledFSIndexStorage.setWorkingSpace(work.getAbsolutePath());
		mu.setJournaledObject(journaledFSIndexStorage);
		mu.setMergeUpdateManager(mergeUpdateManager);

		journaledFSIndexStorage.addIndexListener(new IndexListener()
		{

			public void doIndexReaderClose(IndexReader oldMultiReader, File[] f)
					throws IOException
			{
				if (inclose.get() == null)
				{
					if (oldMultiReader != null)
					{
						try
						{
							inclose.set("inclose");
							oldMultiReader.close();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						finally
						{
							inclose.set(null);
						}
					}
					for (File fs : f)
					{
						try
						{
							FileUtils.deleteAll(fs);
							log.info("Deleted " + fs.getPath());
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					throw new IOException("Imediate close already performed ");
				}

			}

			public void doIndexReaderOpen(IndexReader newMultiReader)
			{

			}

			public void doIndexSearcherClose(IndexSearcher indexSearcher)
					throws IOException
			{
				if (insearcherclose.get() == null)
				{
					if (indexSearcher != null)
					{
						try
						{
							insearcherclose.set("inclose");
							indexSearcher.close();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						finally
						{
							insearcherclose.set(null);
						}
					}
					throw new IOException("Imediate close already performed ");
				}
			}

			public void doIndexSearcherOpen(IndexSearcher indexSearcher)
			{
				
			}

		});

		// index updater

		JournalManagerUpdateTransaction journalManagerUpdateTransaction = new JournalManagerUpdateTransaction();
		MockSearchIndexBuilder mockSearchIndexBuilder = new MockSearchIndexBuilder();
		TransactionIndexManagerImpl transactionIndexManager = new TransactionIndexManagerImpl();
		SearchBuilderQueueManager searchBuilderQueueManager = new SearchBuilderQueueManager();

		transactionIndexManager.setAnalyzerFactory(new StandardAnalyzerFactory());
		transactionIndexManager.setSearchIndexWorkingDirectory(work.getAbsolutePath());
		transactionIndexManager.setSequence(sequence);

		journalManagerUpdateTransaction.setJournalManager(journalManager);

		JournalStorageUpdateTransactionListener journalStorageUpdateTransactionListener = new JournalStorageUpdateTransactionListener();
		journalStorageUpdateTransactionListener
				.setJournalStorage(sharedFilesystemJournalStorage);

		searchBuilderQueueManager.setDatasource(tds.getDataSource());
		searchBuilderQueueManager.setSearchIndexBuilder(mockSearchIndexBuilder);

		transactionIndexManager.addTransactionListener(searchBuilderQueueManager);
		transactionIndexManager
				.addTransactionListener(journalStorageUpdateTransactionListener);
		transactionIndexManager.addTransactionListener(journalManagerUpdateTransaction);

		tiw = new TransactionalIndexWorker();
		tiw.setSearchIndexBuilder(mockSearchIndexBuilder);
		tiw.setServerConfigurationService(serverConfigurationService);
		tiw.setTransactionIndexManager(transactionIndexManager);

		sequence.init();
		searchBuilderQueueManager.init();
		transactionIndexManager.init();
		mu.init();
		journaledFSIndexStorage.init();

		tiw.init();

		
		
		optimizableIndex = new OptimizableIndexImpl();
		optimizableIndex.setJournaledIndex(journaledFSIndexStorage);

		OptimizeTransactionListenerImpl otli = new OptimizeTransactionListenerImpl();
		otli.setMergeSize(5);
		otli.setOptimizableIndex(optimizableIndex);

		OptimizeIndexManager oum = new OptimizeIndexManager();
		oum.setAnalyzerFactory(analyzerFactory);
		oum.setSearchIndexWorkingDirectory(optimizeWork.getAbsolutePath());
		oum.addTransactionListener(otli);
		oum.setSequence(new LocalTransactionSequenceImpl());

		oo = new OptimizeIndexOperation();
		oo.setJournaledObject(journaledFSIndexStorage);
		oo.setOptimizeUpdateManager(oum);

		oo.init();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.optimize.impl.OptimizeIndexOperation#runOnce()}.
	 * 
	 * @throws Exception
	 */
	public final void testRunOnce() throws Exception
	{
		int n = tds.populateDocuments(5000);
		int i = 0;
		while ((n = tiw.process(10)) > 0)
		{
			assertEquals(
					"Runaway Cyclic Indexing, should have completed processing by now ",
					true, i < 500);
			i++;
		}
		log.info("Indexing Complete at " + i + " with " + n);

		// need to populate the index with some data first.
		mu.runOnce();

		File[] optSegments = optimizableIndex.getOptimizableSegments();
		for (File f : optSegments)
		{
			log.info("Segment at " + f.getPath());
		}
		IndexSearcher s = journaledFSIndexStorage.getIndexSearcher();

		log.info("Optimizing ");
		// Now perform an optimize operation
		oo.runOnce();
		log.info("Done Optimizing ");

		optSegments = optimizableIndex.getOptimizableSegments();
		log.info("Number of Temp Segments is " + optSegments.length);
		for (File f : optSegments)
		{
			log.info("Segment at " + f.getPath());
		}

		log.info("Current Index is " + s);
		s = journaledFSIndexStorage.getIndexSearcher();
		log.info("Reopening Index " + s);

		log.info("Index ");
		FileUtils.listDirectory(index);
		log.info("shared ");
		FileUtils.listDirectory(shared);
		log.info("work ");
		FileUtils.listDirectory(work);
		log.info("optimizeWork ");
		FileUtils.listDirectory(optimizeWork);

		s.close();

	}

}
