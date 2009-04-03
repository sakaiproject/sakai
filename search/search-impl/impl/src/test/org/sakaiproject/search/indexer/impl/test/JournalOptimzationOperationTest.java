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

package org.sakaiproject.search.indexer.impl.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.search.index.impl.StandardAnalyzerFactory;
import org.sakaiproject.search.indexer.impl.JournalManagerUpdateTransaction;
import org.sakaiproject.search.indexer.impl.JournalStorageUpdateTransactionListener;
import org.sakaiproject.search.indexer.impl.SearchBuilderQueueManager;
import org.sakaiproject.search.indexer.impl.TransactionIndexManagerImpl;
import org.sakaiproject.search.indexer.impl.TransactionalIndexWorker;
import org.sakaiproject.search.journal.api.IndexCloser;
import org.sakaiproject.search.journal.impl.DbJournalManager;
import org.sakaiproject.search.journal.impl.IndexListenerCloser;
import org.sakaiproject.search.journal.impl.JournalSettings;
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorage;
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorageUpdateTransactionListener;
import org.sakaiproject.search.journal.impl.MergeUpdateManager;
import org.sakaiproject.search.journal.impl.MergeUpdateOperation;
import org.sakaiproject.search.journal.impl.SharedFilesystemJournalStorage;
import org.sakaiproject.search.mock.MockClusterService;
import org.sakaiproject.search.mock.MockSearchIndexBuilder;
import org.sakaiproject.search.mock.MockServerConfigurationService;
import org.sakaiproject.search.mock.MockThreadLocalManager;
import org.sakaiproject.search.optimize.impl.OptimizableIndexImpl;
import org.sakaiproject.search.optimize.impl.OptimizeIndexManager;
import org.sakaiproject.search.optimize.impl.OptimizeIndexOperation;
import org.sakaiproject.search.optimize.impl.OptimizeTransactionListenerImpl;
import org.sakaiproject.search.optimize.shared.impl.DbJournalOptimizationManager;
import org.sakaiproject.search.optimize.shared.impl.JournalOptimizationEndTransactionListener;
import org.sakaiproject.search.optimize.shared.impl.JournalOptimizationManagerImpl;
import org.sakaiproject.search.optimize.shared.impl.JournalOptimizationOperation;
import org.sakaiproject.search.optimize.shared.impl.JournalOptimizationStartTransactionListener;
import org.sakaiproject.search.optimize.shared.impl.OptimizeSharedTransactionListenerImpl;
import org.sakaiproject.search.optimize.shared.impl.SharedFilesystemLoadTransactionListener;
import org.sakaiproject.search.optimize.shared.impl.SharedFilesystemSaveTransactionListener;
import org.sakaiproject.search.transaction.api.TransactionListener;
import org.sakaiproject.search.transaction.impl.LocalTransactionSequenceImpl;
import org.sakaiproject.search.transaction.impl.TransactionSequenceImpl;
import org.sakaiproject.search.util.FileUtils;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb
 */
public class JournalOptimzationOperationTest extends TestCase
{

	private static final Log log = LogFactory
			.getLog(JournalOptimzationOperationTest.class);

	private JournalOptimizationOperation journalOptimizationOperation;

	private File testBase;

	private TDataSource tds;

	private MergeUpdateOperation mu;

	private JournaledFSIndexStorage journaledFSIndexStorage;

	private TransactionalIndexWorker tiw;

	private OptimizableIndexImpl optimizableIndex;

	private OptimizeIndexOperation oo;

	private File journalOptimizeWork;

	private JournalSettings journalSettings;

	private ThreadLocalManager threadLocalManager;

	protected ConcurrentHashMap<IndexCloser, IndexCloser> closeMap = new ConcurrentHashMap<IndexCloser, IndexCloser>();

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

		testBase = new File("target");
		testBase = new File(testBase, "JournalOptimzationOperationTest");

		FileUtils.deleteAll(testBase);

		String localIndexBase = new File(testBase, "local").getAbsolutePath();
		String sharedJournalBase = new File(testBase, "shared").getAbsolutePath();

		threadLocalManager = new MockThreadLocalManager();

		journalSettings = new JournalSettings();
		journalSettings.setLocalIndexBase(localIndexBase);
		journalSettings.setSharedJournalBase(sharedJournalBase);
		journalSettings.setMinimumOptimizeSavePoints(5);
		journalSettings.setOptimizeMergeSize(5);
		journalSettings.setLocalMaxBufferedDocs(50);
		journalSettings.setLocalMaxMergeDocs(1000000);
		journalSettings.setLocalMaxMergeFactor(10);
		journalSettings.setSharedMaxBufferedDocs(50);
		journalSettings.setSharedMaxMergeDocs(1000000);
		journalSettings.setSharedMaxMergeFactor(10);
		journalSettings.setCreateMaxBufferedDocs(50);
		journalSettings.setCreateMaxMergeDocs(1000000);
		journalSettings.setCreateMaxMergeFactor(10);

		journalSettings.setSoakTest(true);

		tds = new TDataSource(5, false);

		mu = new MergeUpdateOperation();
		journaledFSIndexStorage = new JournaledFSIndexStorage();
		journaledFSIndexStorage.setThreadLocalManager(threadLocalManager);

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

		sharedFilesystemJournalStorage.setJournalSettings(journalSettings);

		sequence.setDatasource(tds.getDataSource());

		mergeUpdateManager
				.addTransactionListener(journaledFSIndexStorageUpdateTransactionListener);

		mergeUpdateManager.setSequence(sequence);

		journalManager.setDatasource(tds.getDataSource());
		journalManager.setServerConfigurationService(serverConfigurationService);

		journaledFSIndexStorage.setAnalyzerFactory(analyzerFactory);
		journaledFSIndexStorage.setDatasource(tds.getDataSource());
		journaledFSIndexStorage.setJournalManager(journalManager);
		journaledFSIndexStorage.setRecoverCorruptedIndex(false);
		journaledFSIndexStorage.setJournalSettings(journalSettings);
		journaledFSIndexStorage.setServerConfigurationService(serverConfigurationService);
		mu.setJournaledObject(journaledFSIndexStorage);
		mu.setMergeUpdateManager(mergeUpdateManager);

		optimizableIndex = new OptimizableIndexImpl();
		optimizableIndex.setJournaledIndex(journaledFSIndexStorage);

		OptimizeTransactionListenerImpl otli = new OptimizeTransactionListenerImpl();
		otli.setJournalSettings(journalSettings);
		otli.setOptimizableIndex(optimizableIndex);

		OptimizeIndexManager optimizeUpdateManager = new OptimizeIndexManager();
		optimizeUpdateManager.setAnalyzerFactory(analyzerFactory);
		optimizeUpdateManager.setJournalSettings(journalSettings);
		optimizeUpdateManager.addTransactionListener(otli);
		optimizeUpdateManager.setSequence(new LocalTransactionSequenceImpl());

		oo = new OptimizeIndexOperation();
		oo.setJournaledObject(journaledFSIndexStorage);
		oo.setOptimizeUpdateManager(optimizeUpdateManager);

		mu.setOptimizeUpdateManager(optimizeUpdateManager);

		journaledFSIndexStorage.addIndexListener(new IndexListenerCloser());

		// index updater

		JournalManagerUpdateTransaction journalManagerUpdateTransaction = new JournalManagerUpdateTransaction();
		MockSearchIndexBuilder mockSearchIndexBuilder = new MockSearchIndexBuilder();
		TransactionIndexManagerImpl transactionIndexManager = new TransactionIndexManagerImpl();
		SearchBuilderQueueManager searchBuilderQueueManager = new SearchBuilderQueueManager();

		TransactionSequenceImpl lockSequenceImpl = new TransactionSequenceImpl();
		lockSequenceImpl.setDatasource(tds.getDataSource());
		lockSequenceImpl.setName("queueManagerLock");
		lockSequenceImpl.setMinValue(2000);
		lockSequenceImpl.setMaxValue(10000000);
		lockSequenceImpl.init();
		searchBuilderQueueManager.setSequence(lockSequenceImpl);

		transactionIndexManager.setAnalyzerFactory(new StandardAnalyzerFactory());
		transactionIndexManager.setJournalSettings(journalSettings);
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
		tiw.setThreadLocalManager(threadLocalManager);

		sequence.init();
		searchBuilderQueueManager.init();
		transactionIndexManager.init();
		journalManager.init();
		mu.init();
		journaledFSIndexStorage.init();

		tiw.init();

		oo.init();

		journalOptimizationOperation = new JournalOptimizationOperation();
		JournalOptimizationManagerImpl journalOptimizationManager = new JournalOptimizationManagerImpl();

		JournalOptimizationStartTransactionListener journalOptimizationStartTransactionListener = new JournalOptimizationStartTransactionListener();
		JournalOptimizationEndTransactionListener journalOptimizationEndTransactionListener = new JournalOptimizationEndTransactionListener();
		SharedFilesystemLoadTransactionListener sharedFilesystemLoadTransactionListener = new SharedFilesystemLoadTransactionListener();
		SharedFilesystemSaveTransactionListener sharedFilesystemSaveTransactionListener = new SharedFilesystemSaveTransactionListener();
		sharedFilesystemSaveTransactionListener.setSharedSleep(10);
		OptimizeSharedTransactionListenerImpl optimizeSharedTransactionListener = new OptimizeSharedTransactionListenerImpl();
		optimizeSharedTransactionListener.setJournalSettings(journalSettings);

		DbJournalOptimizationManager optJournalManager = new DbJournalOptimizationManager();
		TransactionSequenceImpl optSequence = new TransactionSequenceImpl();

		MockClusterService clusterService = new MockClusterService();
		clusterService.addServerConfigurationService(serverConfigurationService);

		long journalOptimizeLimit = 5;

		optSequence.setName("journaloptimize");
		optSequence.setDatasource(tds.getDataSource());

		optJournalManager.setClusterService(clusterService);
		optJournalManager.setDatasource(tds.getDataSource());
		optJournalManager.setJournalSettings(journalSettings);
		optJournalManager.setServerConfigurationService(serverConfigurationService);

		sharedFilesystemLoadTransactionListener
				.setSharedFilesystemJournalStorage(sharedFilesystemJournalStorage);
		sharedFilesystemSaveTransactionListener
				.setSharedFilesystemJournalStorage(sharedFilesystemJournalStorage);

		journalOptimizationManager
				.addTransactionListener(journalOptimizationStartTransactionListener);
		journalOptimizationManager
				.addTransactionListener(sharedFilesystemLoadTransactionListener);
		journalOptimizationManager
				.addTransactionListener(optimizeSharedTransactionListener);
		journalOptimizationManager
				.addTransactionListener(sharedFilesystemSaveTransactionListener);
		journalOptimizationManager
				.addTransactionListener(journalOptimizationEndTransactionListener);

		journalOptimizationManager.setAnalyzerFactory(analyzerFactory);
		journalOptimizationManager.setJournalManager(optJournalManager);
		journalOptimizationManager.setSequence(optSequence);
		journalOptimizationManager.setJournalSettings(journalSettings);

		List<TransactionListener> tl = new ArrayList<TransactionListener>();
		tl.add(journalOptimizationStartTransactionListener);
		tl.add(sharedFilesystemLoadTransactionListener);
		tl.add(optimizeSharedTransactionListener);
		tl.add(sharedFilesystemSaveTransactionListener);
		tl.add(journalOptimizationEndTransactionListener);
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
	 * 
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
		tds.populateDocuments(1000, "loadindex");
		int n = 0;
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

	public void listSpaces() throws IOException
	{
		log.info("Index ");
		FileUtils.listDirectory(new File(journalSettings.getLocalIndexBase()));
		log.info("shared ");
		FileUtils.listDirectory(new File(journalSettings.getSharedJournalBase()));

	}

}
