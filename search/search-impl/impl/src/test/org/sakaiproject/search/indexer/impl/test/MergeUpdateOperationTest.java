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
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorage;
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorageUpdateTransactionListener;
import org.sakaiproject.search.journal.impl.MergeUpdateManager;
import org.sakaiproject.search.journal.impl.MergeUpdateOperation;
import org.sakaiproject.search.journal.impl.SharedFilesystemJournalStorage;
import org.sakaiproject.search.mock.MockSearchIndexBuilder;
import org.sakaiproject.search.mock.MockServerConfigurationService;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.transaction.impl.TransactionSequenceImpl;
import org.sakaiproject.search.util.FileUtils;

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
		testBase = new File("m2-target");
		testBase = new File(testBase, "MergeUpdateOperationTest");
		work = new File(testBase, "work");
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
		transactionIndexManager.addTransactionListener(new DebugTransactionListener());

		tiw = new TransactionalIndexWorker();
		tiw.setSearchIndexBuilder(mockSearchIndexBuilder);
		tiw.setServerConfigurationService(serverConfigurationService);
		tiw.setTransactionIndexManager(transactionIndexManager);
		tiw.addIndexWorkerDocumentListener(new DebugIndexWorkerDocumentListener());
		tiw.addIndexWorkerListener(new DebugIndexWorkerListener());

		sequence.init();
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

		List<SearchBuilderItem> items = tds.populateDocuments(1000,"mergeupdate");
		int i = 0;
		int n = 0;
		while ((n = tiw.process(10)) > 0)
		{
			log.info("Processing " + i + " gave " + n);
			assertEquals(
					"Runaway Cyclic Indexing, should have completed processing by now ",
					true, i < 500);
			i++;
		}
		log.info("Indexing Complete at " + i + " with " + n);

		// need to populate the index with some data first.
		mu.runOnce();

		
		
		IndexSearcher indexSearcher = journaledFSIndexStorage.getIndexSearcher();

		assertEquals("There were errors validating the index, check log ", 0,tds.checkIndexContents(items,indexSearcher));

		log.info("==PASSED========================== " + this.getClass().getName()
				+ ".testRunOnce");

	}

}
