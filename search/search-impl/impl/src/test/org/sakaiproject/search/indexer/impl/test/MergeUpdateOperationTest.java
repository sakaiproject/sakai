/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.search.index.impl.StandardAnalyzerFactory;
import org.sakaiproject.search.indexer.debug.DebugIndexWorkerDocumentListener;
import org.sakaiproject.search.indexer.debug.DebugIndexWorkerListener;
import org.sakaiproject.search.indexer.debug.DebugTransactionListener;
import org.sakaiproject.search.indexer.impl.JournalManagerUpdateTransaction;
import org.sakaiproject.search.indexer.impl.JournalStorageUpdateTransactionListener;
import org.sakaiproject.search.indexer.impl.SearchBuilderQueueManager;
import org.sakaiproject.search.indexer.impl.TransactionIndexManagerImpl;
import org.sakaiproject.search.indexer.impl.TransactionalIndexWorker;
import org.sakaiproject.search.journal.impl.DbJournalManager;
import org.sakaiproject.search.journal.impl.IndexListenerCloser;
import org.sakaiproject.search.journal.impl.JournalSettings;
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorage;
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorageUpdateTransactionListener;
import org.sakaiproject.search.journal.impl.MergeUpdateManager;
import org.sakaiproject.search.journal.impl.MergeUpdateOperation;
import org.sakaiproject.search.journal.impl.SharedFilesystemJournalStorage;
import org.sakaiproject.search.mock.MockSearchIndexBuilder;
import org.sakaiproject.search.mock.MockSearchService;
import org.sakaiproject.search.mock.MockServerConfigurationService;
import org.sakaiproject.search.mock.MockThreadLocalManager;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.optimize.impl.OptimizableIndexImpl;
import org.sakaiproject.search.optimize.impl.OptimizeIndexManager;
import org.sakaiproject.search.optimize.impl.OptimizeTransactionListenerImpl;
import org.sakaiproject.search.transaction.impl.LocalTransactionSequenceImpl;
import org.sakaiproject.search.transaction.impl.TransactionSequenceImpl;
import org.sakaiproject.search.util.FileUtils;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb
 */
public class MergeUpdateOperationTest extends TestCase
{

	private static final Log log = LogFactory.getLog(MergeUpdateOperationTest.class);

	private TDataSource tds;

	private File testBase;

	private File work;

	private File shared;

	private File index;

	private MergeUpdateOperation mu;

	private TransactionalIndexWorker tiw;

	private JournaledFSIndexStorage journaledFSIndexStorage;

	private ThreadLocalManager threadLocalManager;

	private IndexListenerCloser indexListenerCloser;

	private File localIndexHolder;

	/**
	 * @param name
	 */
	public MergeUpdateOperationTest(String name)
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
		testBase = new File(testBase, "MergeUpdateOperationTest");
		localIndexHolder = new File(testBase,"localindexholder");

		String localIndexBase = new File(localIndexHolder,"local").getAbsolutePath();
		String sharedJournalBase = new File(testBase,"shared").getAbsolutePath();

		threadLocalManager = new MockThreadLocalManager();

		
		JournalSettings journalSettings = new JournalSettings();
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
		indexListenerCloser = new IndexListenerCloser();
		journaledFSIndexStorage.addIndexListener(indexListenerCloser);
		mu.setJournaledObject(journaledFSIndexStorage);
		mu.setMergeUpdateManager(mergeUpdateManager);
		
		OptimizableIndexImpl optimizableIndex = new OptimizableIndexImpl();
		optimizableIndex.setJournaledIndex(journaledFSIndexStorage);

		
		OptimizeTransactionListenerImpl otli = new OptimizeTransactionListenerImpl();
		otli.setJournalSettings(journalSettings);
		otli.setOptimizableIndex(optimizableIndex);

		OptimizeIndexManager optimizeUpdateManager = new OptimizeIndexManager();
		optimizeUpdateManager.setAnalyzerFactory(analyzerFactory);
		optimizeUpdateManager.setJournalSettings(journalSettings);
		optimizeUpdateManager.addTransactionListener(otli);
		optimizeUpdateManager.setSequence(new LocalTransactionSequenceImpl());

		
		mu.setOptimizeUpdateManager(optimizeUpdateManager);

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
		transactionIndexManager.addTransactionListener(new DebugTransactionListener());

		MockSearchService searchService = new MockSearchService();
		searchService.setDatasource(tds.getDataSource());
		searchService.setServerConfigurationService(new MockServerConfigurationService());
				
		tiw = new TransactionalIndexWorker();
		tiw.setSearchIndexBuilder(mockSearchIndexBuilder);
		tiw.setServerConfigurationService(serverConfigurationService);
		tiw.setTransactionIndexManager(transactionIndexManager);
		tiw.setSearchService(searchService);
		tiw.setThreadLocalManager(threadLocalManager);
		tiw.addIndexWorkerDocumentListener(new DebugIndexWorkerDocumentListener());
		tiw.addIndexWorkerListener(new DebugIndexWorkerListener());

		sequence.init();
		journalManager.init();
		searchBuilderQueueManager.init();
		transactionIndexManager.init();
		mu.init();
		journaledFSIndexStorage.init();

		tiw.init();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
		FileUtils.deleteAll(testBase);
		tds.close();
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.journal.impl.MergeUpdateOperation#runOnce()}.
	 * 
	 * @throws Exception
	 */
	public final void testRunOnce() throws Exception
	{
		log.info("================================== " + this.getClass().getName()
				+ ".testRunOnce");
		List<SearchBuilderItem> items = populate(1000,100);

		log.debug("finished populating data");
		
		// need to populate the index with some data first.
		mu.runOnce();

		log.debug("finished runOnce");
		
		IndexSearcher indexSearcher = journaledFSIndexStorage.getIndexSearcher();

		log.debug("finished getIndexSearcher");
		
		assertEquals("There were errors validating the index, check log ", 0,tds.checkIndexContents(items,indexSearcher));

		indexSearcher.close();
		
		log.info("==PASSED========================== " + this.getClass().getName()
				+ ".testRunOnce");

	}
	
	private List<SearchBuilderItem> populate(int nitems, int step) throws Exception {
		List<SearchBuilderItem> items = tds.populateDocuments(nitems,"mergeupdate");
		int i = 0;
		int n = 0;
		while ((n = tiw.process(step)) > 0)
		{
			log.info("Processing " + i + " gave " + n);
			assertEquals(
					"Runaway Cyclic Indexing, should have completed processing by now ",
					true, i < 500);
			i++;
		}
		log.info("Indexing Complete at " + i + " with " + n);
		return items;
		
	}
	
	public void disableRecoveryMergeOperation() throws Exception  {
		log.info("================================== " + this.getClass().getName()
				+ ".testRevoceryMergeOperation");
		List<SearchBuilderItem> items = populate(10000,2);
		checkForOpenIndexes();
		mu.runOnce();
		checkForOpenIndexes();
		// bin the local index storage space
		FileUtils.deleteAll(localIndexHolder);
		// bring up the merge operation and add some more items
		mu.runOnce();
		checkForOpenIndexes();
		IndexSearcher indexSearcher = journaledFSIndexStorage.getIndexSearcher();
		checkForOpenIndexes();
		assertEquals("There were errors validating the index, check log ", 0,tds.checkIndexContents(items,indexSearcher));
		log.info("==PASSED========================== " + this.getClass().getName()
				+ ".testRevoceryMergeOperation");
	}

	/**
	 * 
	 */
	private void checkForOpenIndexes()
	{
		int opened = indexListenerCloser.size();
		if ( opened > 0 ) {
			for ( String s : indexListenerCloser.getOpenIndexNames() ) {
				log.info("Found Open index "+s);
			}
			fail("Leaking Open Indexes "+opened);
		}
	}

}
