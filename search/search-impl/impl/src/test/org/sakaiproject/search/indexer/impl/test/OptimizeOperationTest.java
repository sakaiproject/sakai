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

package org.sakaiproject.search.indexer.impl.test;

import java.io.File;
import java.util.List;

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
import org.sakaiproject.search.journal.impl.DbJournalManager;
import org.sakaiproject.search.journal.impl.IndexListenerCloser;
import org.sakaiproject.search.journal.impl.JournalSettings;
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorage;
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorageUpdateTransactionListener;
import org.sakaiproject.search.journal.impl.MergeUpdateManager;
import org.sakaiproject.search.journal.impl.MergeUpdateOperation;
import org.sakaiproject.search.journal.impl.SharedFilesystemJournalStorage;
import org.sakaiproject.search.mock.MockSearchIndexBuilder;
import org.sakaiproject.search.mock.MockServerConfigurationService;
import org.sakaiproject.search.mock.MockThreadLocalManager;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.optimize.impl.OptimizableIndexImpl;
import org.sakaiproject.search.optimize.impl.OptimizeIndexManager;
import org.sakaiproject.search.optimize.impl.OptimizeIndexOperation;
import org.sakaiproject.search.optimize.impl.OptimizeTransactionListenerImpl;
import org.sakaiproject.search.transaction.impl.LocalTransactionSequenceImpl;
import org.sakaiproject.search.transaction.impl.TransactionSequenceImpl;
import org.sakaiproject.search.util.FileUtils;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb
 */
public class OptimizeOperationTest extends TestCase
{

	private static final Log log = LogFactory.getLog(OptimizeOperationTest.class);

	private File testBase;

	private TDataSource tds;

	private MergeUpdateOperation mu;

	private TransactionalIndexWorker tiw;

	private OptimizeIndexOperation oo;

	private File optimizeWork;

	private OptimizableIndexImpl optimizableIndex;

	private JournaledFSIndexStorage journaledFSIndexStorage;

	protected ThreadLocal<Object> inclose = new ThreadLocal<Object>();

	protected ThreadLocal<Object> insearcherclose = new ThreadLocal<Object>();

	private JournalSettings journalSettings;

	private ThreadLocalManager threadLocalManager;

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
		testBase = new File("target");
		testBase = new File(testBase, "OptimizeOperationTest");
		
		
		FileUtils.deleteAll(testBase);
		
		String localIndexBase = new File(testBase,"local").getAbsolutePath();
		String sharedJournalBase = new File(testBase,"shared").getAbsolutePath();

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
		
		
		
		optimizableIndex = new OptimizableIndexImpl();
		optimizableIndex.setJournaledIndex(journaledFSIndexStorage);

		OptimizeTransactionListenerImpl otli = new OptimizeTransactionListenerImpl();
		otli.setJournalSettings(journalSettings);
		otli.setOptimizableIndex(optimizableIndex);

		OptimizeIndexManager oum = new OptimizeIndexManager();
		oum.setAnalyzerFactory(analyzerFactory);
		oum.setJournalSettings(journalSettings);
		oum.addTransactionListener(otli);
		oum.setSequence(new LocalTransactionSequenceImpl());

		oo = new OptimizeIndexOperation();
		oo.setJournaledObject(journaledFSIndexStorage);
		oo.setOptimizeUpdateManager(oum);
		
		mu.setMergeUpdateManager(mergeUpdateManager);
		mu.setOptimizeUpdateManager(oum);
		

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
		List<SearchBuilderItem> items = tds.populateDocuments(1000,"optimizeop");

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
		mu.runOnce();

		assertEquals(
				"Index is not correct after a merge update operationadd items only ", 0,
				tds.checkIndexContents(items, journaledFSIndexStorage.getIndexSearcher()));

		items = tds.deleteSomeItems(items);
		while ((n = tiw.process(10)) > 0)
		{
			assertEquals(
					"Runaway Cyclic Indexing, should have completed processing by now ",
					true, i < 500);
			i++;
		}
		log.info("Indexing Complete at " + i + " with " + n);

		mu.runOnce();

		// need to populate the index with some data first.
		assertEquals(
				"Index is not correct after a merge update operation with deleted items, delete processing is not working ",
				0, tds.checkIndexContents(items, journaledFSIndexStorage
						.getIndexSearcher()));

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

		int errors = tds.checkIndexContents(items, s);
		
		assertEquals(
				"Check on index contents failed, Merge Index Failure on Local Node, please check logs ",
				0, errors);

		
		
		// Generate a second set and do the process again
		List<SearchBuilderItem> items2 = tds.populateDocuments(1000,"set2");

		int n2 = 0;
		int i2 = 0;
		while ((n2 = tiw.process(10)) > 0)
		{
			assertEquals(
					"Runaway Cyclic Indexing, should have completed processing by now ",
					true, i2 < 500);
			i2++;
		}
		log.info("Indexing Complete at " + i2 + " with " + n2);
		mu.runOnce();

		assertEquals(
				"Index is not correct after a merge update operationadd items only ", 0,
				tds.checkIndexContents(items, journaledFSIndexStorage.getIndexSearcher()));
		assertEquals(
				"Index is not correct after a merge update operationadd items only ", 0,
				tds.checkIndexContents(items2, journaledFSIndexStorage.getIndexSearcher()));

		items2 = tds.deleteSomeItems(items2);
		while ((n2 = tiw.process(10)) > 0)
		{
			assertEquals(
					"Runaway Cyclic Indexing, should have completed processing by now ",
					true, i2 < 500);
			i2++;
		}
		log.info("Indexing Complete at " + i2 + " with " + n2);

		mu.runOnce();

		// need to populate the index with some data first.
		assertEquals(
				"Index is not correct after a merge update operation with deleted items, delete processing is not working ",
				0, tds.checkIndexContents(items, journaledFSIndexStorage
						.getIndexSearcher()));
		assertEquals(
				"Index is not correct after a merge update operation with deleted items, delete processing is not working ",
				0, tds.checkIndexContents(items2, journaledFSIndexStorage
						.getIndexSearcher()));

		File[] optSegments2 = optimizableIndex.getOptimizableSegments();
		for (File f : optSegments2)
		{
			log.info("Segment at " + f.getPath());
		}
		IndexSearcher s2 = journaledFSIndexStorage.getIndexSearcher();

		log.info("Optimizing ");
		// Now perform an optimize operation
		oo.runOnce();
		log.info("Done Optimizing ");

		optSegments2 = optimizableIndex.getOptimizableSegments();
		log.info("Number of Temp Segments is " + optSegments2.length);
		for (File f : optSegments2)
		{
			log.info("Segment at " + f.getPath());
		}

		log.info("Current Index is " + s2);
		s2 = journaledFSIndexStorage.getIndexSearcher();
		log.info("Reopening Index " + s2);

		int errors2 = tds.checkIndexContents(items, s2);
		errors2 += tds.checkIndexContents(items2, s2);
		
		

		log.info("Local ");
		FileUtils.listDirectory(new File(journalSettings.getLocalIndexBase()));
		log.info("shared ");
		FileUtils.listDirectory(new File(journalSettings.getSharedJournalBase()));

		s.close();
		s2.close();
		assertEquals(
				"Check on index contents failed, Merge Index Failure on Local Node, please check logs ",
				0, errors2);
	}

}
