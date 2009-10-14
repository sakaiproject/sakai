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

package org.sakaiproject.search.index.soaktest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.component.service.impl.SearchServiceImpl;
import org.sakaiproject.search.index.impl.StandardAnalyzerFactory;
import org.sakaiproject.search.indexer.debug.DebugIndexWorkerListener;
import org.sakaiproject.search.indexer.impl.ConcurrentSearchIndexBuilderWorkerImpl;
import org.sakaiproject.search.indexer.impl.JournalManagerUpdateTransaction;
import org.sakaiproject.search.indexer.impl.JournalStorageUpdateTransactionListener;
import org.sakaiproject.search.indexer.impl.SearchBuilderQueueManager;
import org.sakaiproject.search.indexer.impl.TransactionIndexManagerImpl;
import org.sakaiproject.search.indexer.impl.TransactionalIndexWorker;
import org.sakaiproject.search.journal.api.ManagementOperation;
import org.sakaiproject.search.journal.impl.ConcurrentIndexManager;
import org.sakaiproject.search.journal.impl.DbJournalManager;
import org.sakaiproject.search.journal.impl.IndexListenerCloser;
import org.sakaiproject.search.journal.impl.IndexManagementTimerTask;
import org.sakaiproject.search.journal.impl.JournalSettings;
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorage;
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorageUpdateTransactionListener;
import org.sakaiproject.search.journal.impl.MergeUpdateManager;
import org.sakaiproject.search.journal.impl.MergeUpdateOperation;
import org.sakaiproject.search.journal.impl.SharedFilesystemJournalStorage;
import org.sakaiproject.search.mbeans.SearchServiceManagement;
import org.sakaiproject.search.mock.MockClusterService;
import org.sakaiproject.search.mock.MockComponentManager;
import org.sakaiproject.search.mock.MockEventTrackingService;
import org.sakaiproject.search.mock.MockSearchIndexBuilder;
import org.sakaiproject.search.mock.MockSearchService;
import org.sakaiproject.search.mock.MockSecurityService;
import org.sakaiproject.search.mock.MockServerConfigurationService;
import org.sakaiproject.search.mock.MockSessionManager;
import org.sakaiproject.search.mock.MockThreadLocalManager;
import org.sakaiproject.search.mock.MockUserDirectoryService;
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
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb
 */
public class SearchIndexerNode
{

	protected static final Log log = LogFactory.getLog(SearchIndexerNode.class);

	private SharedTestDataSource tds;

	private MergeUpdateOperation mu;

	private TransactionalIndexWorker tiw;

	private String instanceName;

	private String base;

	private ConcurrentIndexManager cim;

	private String driver;

	private String url;

	private String userame;

	private String password;

	private JournaledFSIndexStorage journaledFSIndexStorage;

	private MockClusterService clusterService;

	private ThreadLocalManager threadLocalManager;

	private TransactionSequenceImpl sequence;

	private static Random rand = new Random();

	public SearchIndexerNode(MockClusterService clusterService, String base,
			String instanceName, String driver, String url, String userame,
			String password)
	{
		this.base = base;
		this.instanceName = instanceName;
		this.driver = driver;
		this.url = url;
		this.userame = userame;
		this.password = password;
		this.clusterService = clusterService;
	}

	public void init() throws Exception
	{
		String shared = base + "/shared";
		String instanceBase = base + "/" + instanceName;
		String localIndexBase = instanceBase + "/index/local";
		String sharedJournalBase = shared;

		threadLocalManager = new MockThreadLocalManager();

		JournalSettings journalSettings = new JournalSettings();
		journalSettings.setLocalIndexBase(localIndexBase);
		journalSettings.setSharedJournalBase(sharedJournalBase);
		journalSettings.setMinimumOptimizeSavePoints(50); // the number of
															// shared save
															// points before we
															// attemt to merge
		journalSettings.setOptimizeMergeSize(50); // the number of local save
													// points before we try to
													// merge
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

		tds = new SharedTestDataSource(base, 10, false, driver, url, userame, password);

		mu = new MergeUpdateOperation();
		journaledFSIndexStorage = new JournaledFSIndexStorage();
		journaledFSIndexStorage.setThreadLocalManager(threadLocalManager);
		StandardAnalyzerFactory analyzerFactory = new StandardAnalyzerFactory();
		DbJournalManager journalManager = new DbJournalManager();
		MockServerConfigurationService serverConfigurationService = new MockServerConfigurationService();
		serverConfigurationService.setInstanceName(instanceName);
		MergeUpdateManager mergeUpdateManager = new MergeUpdateManager();
		sequence = new TransactionSequenceImpl();

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

		transactionIndexManager
				.addTransactionListener(journalStorageUpdateTransactionListener);
		transactionIndexManager.addTransactionListener(searchBuilderQueueManager);
		transactionIndexManager.addTransactionListener(journalManagerUpdateTransaction);
		// transactionIndexManager.addTransactionListener(new
		// DebugTransactionListener());

		
		
		
		MockSearchService searchService = new MockSearchService();
		searchService.setDatasource(tds.getDataSource());
		searchService.setServerConfigurationService(new MockServerConfigurationService());
		
		tiw = new TransactionalIndexWorker();
		tiw.setSearchIndexBuilder(mockSearchIndexBuilder);
		tiw.setServerConfigurationService(serverConfigurationService);
		tiw.setTransactionIndexManager(transactionIndexManager);
		tiw.setSearchService(searchService);
		tiw.setThreadLocalManager(threadLocalManager);
		// tiw.addIndexWorkerDocumentListener(new
		// DebugIndexWorkerDocumentListener());
		tiw.addIndexWorkerListener(new DebugIndexWorkerListener());

		sequence.init();
		searchBuilderQueueManager.init();
		transactionIndexManager.init();
		journalManager.init();
		mu.init();
		journaledFSIndexStorage.init();

		tiw.init();

		OptimizeIndexOperation oo = new OptimizeIndexOperation();
		oo.setJournaledObject(journaledFSIndexStorage);
		oo.setOptimizeUpdateManager(optimizeUpdateManager);

		oo.init();

		cim = new ConcurrentIndexManager();
		IndexManagementTimerTask indexer = new IndexManagementTimerTask();
		IndexManagementTimerTask merger = new IndexManagementTimerTask();
		IndexManagementTimerTask optimizer = new IndexManagementTimerTask();
		
		indexer.setThreadLocalManager(threadLocalManager);
		merger.setThreadLocalManager(threadLocalManager);
		optimizer.setThreadLocalManager(threadLocalManager);

		MockComponentManager componentManager = new MockComponentManager();
		MockEventTrackingService eventTrackingService = new MockEventTrackingService();
		
		MockSessionManager sessionManager = new MockSessionManager();
		MockUserDirectoryService userDirectoryService = new MockUserDirectoryService();
		MockSecurityService securityService = new MockSecurityService();

		ConcurrentSearchIndexBuilderWorkerImpl csibw = new ConcurrentSearchIndexBuilderWorkerImpl();
		csibw.setComponentManager(componentManager);
		csibw.setEventTrackingService(eventTrackingService);
		csibw.setIndexWorker(tiw);
		csibw.setSearchService(searchService);
		csibw.setSecurityService(securityService);
		csibw.setJournalSettings(journalSettings);
		csibw.setUserDirectoryService(userDirectoryService);
		csibw.setServerConfigurationService(serverConfigurationService);
		csibw.init();

		indexer.setManagementOperation(csibw);
		merger.setManagementOperation(mu);
		optimizer.setManagementOperation(oo);

		List<IndexManagementTimerTask> taskList = new ArrayList<IndexManagementTimerTask>();
		taskList.add(indexer);
		indexer.setDelay(10);
		indexer.setPeriod(1000);
		taskList.add(merger);
		merger.setDelay(10);
		merger.setPeriod(10000);
		taskList.add(optimizer);
		optimizer.setDelay(50);
		optimizer.setPeriod(10000);

		IndexManagementTimerTask docloader = new IndexManagementTimerTask();
		docloader.setThreadLocalManager(threadLocalManager);
		docloader.setDelay(5);
		docloader.setPeriod(500);
		docloader.setManagementOperation(new ManagementOperation()
		{

			public void runOnce()
			{
				try
				{
					tds.populateDocuments(500, instanceName);
					tds.resetAfter(3000);

				}
				catch (Exception e)
				{
					log.error("Failed to LoadDocuments ", e);
				}
			}

		});

		taskList.add(docloader);

		journaledFSIndexStorage.addIndexListener(cim);
		IndexListenerCloser indexCloser = new IndexListenerCloser();
		journaledFSIndexStorage.addIndexListener(indexCloser);

		// Journal Optimization

		JournalOptimizationOperation journalOptimizationOperation = new JournalOptimizationOperation();
		JournalOptimizationManagerImpl journalOptimizationManager = new JournalOptimizationManagerImpl();

		JournalOptimizationStartTransactionListener journalOptimizationStartTransactionListener = new JournalOptimizationStartTransactionListener();
		JournalOptimizationEndTransactionListener journalOptimizationEndTransactionListener = new JournalOptimizationEndTransactionListener();
		SharedFilesystemLoadTransactionListener sharedFilesystemLoadTransactionListener = new SharedFilesystemLoadTransactionListener();
		SharedFilesystemSaveTransactionListener sharedFilesystemSaveTransactionListener = new SharedFilesystemSaveTransactionListener();
		sharedFilesystemSaveTransactionListener.setSharedSleep(120000);
		OptimizeSharedTransactionListenerImpl optimizeSharedTransactionListener = new OptimizeSharedTransactionListenerImpl();
		optimizeSharedTransactionListener.setJournalSettings(journalSettings);

		DbJournalOptimizationManager optimizeJournalManager = new DbJournalOptimizationManager();
		TransactionSequenceImpl optimizeSequence = new TransactionSequenceImpl();
		sequence.getClass();
		clusterService.addServerConfigurationService(serverConfigurationService);

		long journalOptimizeLimit = 100;

		optimizeSequence.setName("journaloptimize");
		optimizeSequence.setDatasource(tds.getDataSource());

		optimizeJournalManager.setClusterService(clusterService);
		optimizeJournalManager.setDatasource(tds.getDataSource());
		optimizeJournalManager.setJournalSettings(journalSettings);
		optimizeJournalManager.setServerConfigurationService(serverConfigurationService);

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
		journalOptimizationManager.setJournalManager(optimizeJournalManager);
		journalOptimizationManager.setSequence(optimizeSequence);
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
		journalOptimizationOperation.setServerConfigurationService(serverConfigurationService);

		optimizeJournalManager.init();
		optimizeSequence.init();
		journalOptimizationOperation.init();

		IndexManagementTimerTask journalOptimizer = new IndexManagementTimerTask();
		journalOptimizer.setThreadLocalManager(threadLocalManager);
		journalOptimizer.setManagementOperation(journalOptimizationOperation);
		journalOptimizer.setDelay(10);
		journalOptimizer.setPeriod(1000);
		taskList.add(journalOptimizer);

		SearchServiceManagement mbean = new SearchServiceManagement(instanceName);
		mbean.setIndexStorageProvider(journaledFSIndexStorage);
		mbean.setSearchService(searchService);

		mbean.setIndexListenerCloser(indexCloser);
		mbean.setIndexWorker(tiw);
		mbean.setThreadLocalManager(threadLocalManager);

		mbean.init();

		cim.setSearchService(searchService);
		cim.setTasks(taskList);
		cim.init();

	}

	/**
	 * @throws Exception
	 */
	public void close() throws Exception
	{
		cim.close();

	}

	public void cleanup() throws Exception
	{
		cim.cleanup();
		tds.close();

	}

	/**
	 * @throws IOException
	 */
	public void testSearch()
	{
		try
		{
			long start1 = System.currentTimeMillis();
			IndexSearcher is = journaledFSIndexStorage.getIndexSearcher();
			TermQuery tq = new TermQuery(new Term(SearchService.FIELD_CONTENTS, "node"));

			long start = System.currentTimeMillis();
			Hits h = is.search(tq);
			long end = System.currentTimeMillis();
			log.debug("Got " + h.length() + " hits from " + is.getIndexReader().numDocs()
					+ " for node " + instanceName + " in " + (end - start) + ":"
					+ (start - start1) + " ms");
		}
		catch (Exception ex)
		{
			log.error("Search Failed with, perhapse due to a file being removed "
					+ ex.getMessage());
		}
	}

	public void testSlowSearch() throws Exception
	{
		long start1 = System.currentTimeMillis();

		log.debug("Getting index searcher");
		IndexSearcher is = journaledFSIndexStorage.getIndexSearcher();
		TermQuery tq = new TermQuery(new Term(SearchService.FIELD_CONTENTS, "node"));

		long start = System.currentTimeMillis();
		log.info("Searching with " + is + " and reader " + is.getIndexReader());
		Hits h = is.search(tq);
		log.info("Performing Search and Sleeping 500ms with " + is);
		Thread.sleep(500);
		log.info("Performing Search and Sleeping 500ms with " + is);
		long end = System.currentTimeMillis();
		log.info("Got " + h.length() + " hits from " + is.getIndexReader().numDocs()
				+ " for node " + instanceName + " in " + (end - start) + ":"
				+ (start - start1) + " ms");
		for (int i = 0; i < h.length(); i++)
		{
			Document d = h.doc(i);
			for (Enumeration<Field> e = d.fields(); e.hasMoreElements();)
			{
				e.nextElement();
			}
		}
	}

	/**
	 * @throws IOException
	 */
	public void reopenIndex() throws IOException
	{
		journaledFSIndexStorage.markModified();
		journaledFSIndexStorage.getIndexReader();
		log.info("Reopened Index");

	}

	/**
	 * @return
	 */
	public ThreadLocalManager getThreadLocalManager()
	{
		return threadLocalManager;
	}

	/**
	 * 
	 */
	public void makeTransactionHole()
	{
		if ((rand.nextInt(100) % 18) == 0)
		{
			long n = sequence.getNextId();
			log.info("Simulated Transaction Hole at " + n);
		}
	}

}
