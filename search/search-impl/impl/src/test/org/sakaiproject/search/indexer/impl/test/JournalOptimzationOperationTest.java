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
import java.util.ArrayList;
import java.util.List;

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
import org.sakaiproject.search.mock.MockClusterService;
import org.sakaiproject.search.mock.MockSearchIndexBuilder;
import org.sakaiproject.search.mock.MockServerConfigurationService;
import org.sakaiproject.search.optimize.impl.OptimizableIndexImpl;
import org.sakaiproject.search.optimize.impl.OptimizeIndexManager;
import org.sakaiproject.search.optimize.impl.OptimizeIndexOperation;
import org.sakaiproject.search.optimize.impl.OptimizeTransactionListenerImpl;
import org.sakaiproject.search.optimize.shared.impl.DbJournalOptimizationManager;
import org.sakaiproject.search.optimize.shared.impl.JournalOptimizationManagerImpl;
import org.sakaiproject.search.optimize.shared.impl.JournalOptimizationOperation;
import org.sakaiproject.search.optimize.shared.impl.JournalOptimizationTransactionListener;
import org.sakaiproject.search.optimize.shared.impl.OptimizeSharedTransactionListenerImpl;
import org.sakaiproject.search.optimize.shared.impl.SharedFilesystemLoadTransactionListener;
import org.sakaiproject.search.optimize.shared.impl.SharedFilesystemSaveTransactionListener;
import org.sakaiproject.search.transaction.api.TransactionListener;
import org.sakaiproject.search.transaction.impl.LocalTransactionSequenceImpl;
import org.sakaiproject.search.transaction.impl.TransactionSequenceImpl;
import org.sakaiproject.search.util.FileUtils;

/**
 * @author ieb
 */
public class JournalOptimzationOperationTest extends TestCase
{

	private static final Log log = LogFactory
			.getLog(JournalOptimzationOperationTest.class);

	private JournalOptimizationOperation journalOptimizationOperation;

	private File testBase;

	private File work;

	private File optimizeWork;

	private File shared;

	private File index;

	private TDataSource tds;

	private MergeUpdateOperation mu;

	private JournaledFSIndexStorage journaledFSIndexStorage;

	protected ThreadLocal<Object> inclose = new ThreadLocal<Object>();

	protected ThreadLocal<Object> insearcherclose = new ThreadLocal<Object>();

	private TransactionalIndexWorker tiw;

	private OptimizableIndexImpl optimizableIndex;

	private OptimizeIndexOperation oo;

	private File journalOptimizeWork;

	/**
	 * @param name
	 */
	public JournalOptimzationOperationTest(String name)
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
		testBase = new File(testBase, "JournalOptimzationOperationTest");
		work = new File(testBase, "work");
		optimizeWork = new File(testBase, "optwork");
		journalOptimizeWork = new File(testBase, "optwork");
		shared = new File(testBase, "shared");
		index = new File(testBase, "index");

		FileUtils.deleteAll(testBase);

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

		
		
		
		
		
		
		journalOptimizationOperation = new JournalOptimizationOperation();
		JournalOptimizationManagerImpl journalOptimizationManager = new JournalOptimizationManagerImpl();

		JournalOptimizationTransactionListener journalOptimizationTransactionListener = new JournalOptimizationTransactionListener();
		SharedFilesystemLoadTransactionListener sharedFilesystemLoadTransactionListener = new SharedFilesystemLoadTransactionListener();
		SharedFilesystemSaveTransactionListener sharedFilesystemSaveTransactionListener = new SharedFilesystemSaveTransactionListener();
		OptimizeSharedTransactionListenerImpl optimizeSharedTransactionListener = new OptimizeSharedTransactionListenerImpl();

		DbJournalOptimizationManager optJournalManager = new DbJournalOptimizationManager();
		TransactionSequenceImpl optSequence = new TransactionSequenceImpl();

		MockClusterService clusterService = new MockClusterService();
		clusterService.addServerConfigurationService(serverConfigurationService);

		String journalLocation = shared.getAbsolutePath();

		long journalOptimizeLimit = 5;

		optSequence.setName("journaloptimize");
		optSequence.setDatasource(tds.getDataSource());

		optJournalManager.setClusterService(clusterService);
		optJournalManager.setDatasource(tds.getDataSource());
		optJournalManager.setJournalOptimizeLimit(journalOptimizeLimit);
		optJournalManager.setServerConfigurationService(serverConfigurationService);


		sharedFilesystemLoadTransactionListener
				.setSharedFilesystemJournalStorage(sharedFilesystemJournalStorage);
		sharedFilesystemSaveTransactionListener
				.setSharedFilesystemJournalStorage(sharedFilesystemJournalStorage);

		journalOptimizationManager
				.addTransactionListener(journalOptimizationTransactionListener);
		journalOptimizationManager
				.addTransactionListener(sharedFilesystemLoadTransactionListener);
		journalOptimizationManager
				.addTransactionListener(optimizeSharedTransactionListener);
		journalOptimizationManager
				.addTransactionListener(sharedFilesystemSaveTransactionListener);

		journalOptimizationManager.setAnalyzerFactory(analyzerFactory);
		journalOptimizationManager.setJournalManager(optJournalManager);
		journalOptimizationManager.setSequence(optSequence);
		journalOptimizationManager.setWorkingSpace(journalOptimizeWork.getAbsolutePath());

		List<TransactionListener> tl = new ArrayList<TransactionListener>();
		tl.add(journalOptimizationTransactionListener);
		tl.add(sharedFilesystemLoadTransactionListener);
		tl.add(optimizeSharedTransactionListener);
		tl.add(sharedFilesystemSaveTransactionListener);
		journalOptimizationManager.setTransactionListeners(tl);

		journalOptimizationOperation
				.setJournalOptimizationManager(journalOptimizationManager);

		optJournalManager.init();
		optSequence.init();
		clusterService.init();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
		tds.close();

	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.optimize.shared.impl.JournalOptimizationOperation#init()}.
	 */
	public final void testInit()
	{
		log.info("================================== " + this.getClass().getName()
				+ ".testInit");
		journalOptimizationOperation.init();
		log.info("=PASSED=========================== " + this.getClass().getName()
				+ ".testInit");
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.optimize.shared.impl.JournalOptimizationOperation#runOnce()}.
	 * @throws Exception 
	 */
	public final void testRunOnce() throws Exception
	{
		
		log.info("================================== " + this.getClass().getName()
				+ ".testRunOnce");
		log.info("=SETUP========================== " + this.getClass().getName()
				+ ".testRunOnce");
		loadIndex();
		listSpaces();
		log.info("=SETUP=COMPLETE===TESTING======= " + this.getClass().getName()
				+ ".testRunOnce");
		journalOptimizationOperation.init();
		journalOptimizationOperation.runOnce();
		listSpaces();
		log.info("=PASSED=========================== " + this.getClass().getName()
				+ ".testRunOnce");
	}

	public final void loadIndex() throws Exception
	{
		int n = tds.populateDocuments(1000);
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



	}

	public void listSpaces() throws IOException {
		log.info("Index ");
		FileUtils.listDirectory(index);
		log.info("shared ");
		FileUtils.listDirectory(shared);
		log.info("work ");
		FileUtils.listDirectory(work);
		log.info("optimizeWork ");
		FileUtils.listDirectory(optimizeWork);
		log.info("optimizeWork ");
		FileUtils.listDirectory(journalOptimizeWork);
		
	}
	
}
