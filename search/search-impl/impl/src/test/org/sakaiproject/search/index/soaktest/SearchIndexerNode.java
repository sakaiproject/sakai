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

package org.sakaiproject.search.index.soaktest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.index.AnalyzerFactory;
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
import org.sakaiproject.search.journal.impl.IndexManagementTimerTask;
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorage;
import org.sakaiproject.search.journal.impl.JournaledFSIndexStorageUpdateTransactionListener;
import org.sakaiproject.search.journal.impl.MergeUpdateManager;
import org.sakaiproject.search.journal.impl.MergeUpdateOperation;
import org.sakaiproject.search.journal.impl.SharedFilesystemJournalStorage;
import org.sakaiproject.search.mock.MockClusterService;
import org.sakaiproject.search.mock.MockComponentManager;
import org.sakaiproject.search.mock.MockEventTrackingService;
import org.sakaiproject.search.mock.MockSearchIndexBuilder;
import org.sakaiproject.search.mock.MockSearchService;
import org.sakaiproject.search.mock.MockServerConfigurationService;
import org.sakaiproject.search.mock.MockSessionManager;
import org.sakaiproject.search.mock.MockUserDirectoryService;
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

	public SearchIndexerNode(String base, String instanceName, String driver, String url, String userame, String password) 
	{
		this.base = base;
		this.instanceName = instanceName;
		this.driver = driver;
		this.url = url;
		this.userame = userame;
		this.password = password;
	}

	public void init() throws Exception
	{
		String shared = base + "/shared";
		String instanceBase = base + "/" + instanceName;
		String indexerwork = instanceBase + "/indexerwork";
		String indexwork = instanceBase + "/index/work";
		String index = instanceBase + "/index/main";
		String optimizeWork = instanceBase + "/index/optwork";

		tds = new SharedTestDataSource(base,10,false,driver, url, userame, password);

		mu = new MergeUpdateOperation();
		 journaledFSIndexStorage = new JournaledFSIndexStorage();
		StandardAnalyzerFactory analyzerFactory = new StandardAnalyzerFactory();
		DbJournalManager journalManager = new DbJournalManager();
		MockServerConfigurationService serverConfigurationService = new MockServerConfigurationService();
		serverConfigurationService.setInstanceName(instanceName);
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

		sharedFilesystemJournalStorage.setJournalLocation(shared);

		sequence.setDatasource(tds.getDataSource());

		mergeUpdateManager
				.addTransactionListener(journaledFSIndexStorageUpdateTransactionListener);

		mergeUpdateManager.setSequence(sequence);

		journalManager.setDatasource(tds.getDataSource());

		journaledFSIndexStorage.setAnalyzerFactory(analyzerFactory);
		journaledFSIndexStorage.setDatasource(tds.getDataSource());
		journaledFSIndexStorage.setJournalManager(journalManager);
		journaledFSIndexStorage.setRecoverCorruptedIndex(false);
		journaledFSIndexStorage.setSearchIndexDirectory(index);
		journaledFSIndexStorage.setServerConfigurationService(serverConfigurationService);
		journaledFSIndexStorage.setWorkingSpace(indexwork);
		mu.setJournaledObject(journaledFSIndexStorage);
		mu.setMergeUpdateManager(mergeUpdateManager);

		JournalManagerUpdateTransaction journalManagerUpdateTransaction = new JournalManagerUpdateTransaction();
		MockSearchIndexBuilder mockSearchIndexBuilder = new MockSearchIndexBuilder();
		TransactionIndexManagerImpl transactionIndexManager = new TransactionIndexManagerImpl();
		SearchBuilderQueueManager searchBuilderQueueManager = new SearchBuilderQueueManager();

		transactionIndexManager.setAnalyzerFactory(new StandardAnalyzerFactory());
		transactionIndexManager.setSearchIndexWorkingDirectory(indexerwork);
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
//		transactionIndexManager.addTransactionListener(new DebugTransactionListener());

		tiw = new TransactionalIndexWorker();
		tiw.setSearchIndexBuilder(mockSearchIndexBuilder);
		tiw.setServerConfigurationService(serverConfigurationService);
		tiw.setTransactionIndexManager(transactionIndexManager);
//		tiw.addIndexWorkerDocumentListener(new DebugIndexWorkerDocumentListener());
		tiw.addIndexWorkerListener(new DebugIndexWorkerListener());

		sequence.init();
		searchBuilderQueueManager.init();
		transactionIndexManager.init();
		mu.init();
		journaledFSIndexStorage.init();

		tiw.init();
		
		
		OptimizableIndexImpl optimizableIndex = new OptimizableIndexImpl();
		optimizableIndex.setJournaledIndex(journaledFSIndexStorage);

		OptimizeTransactionListenerImpl otli = new OptimizeTransactionListenerImpl();
		otli.setMergeSize(5);
		otli.setOptimizableIndex(optimizableIndex);

		OptimizeIndexManager oum = new OptimizeIndexManager();
		oum.setAnalyzerFactory(analyzerFactory);
		oum.setSearchIndexWorkingDirectory(optimizeWork);
		oum.addTransactionListener(otli);
		oum.setSequence(new LocalTransactionSequenceImpl());

		OptimizeIndexOperation oo = new OptimizeIndexOperation();
		oo.setJournaledObject(journaledFSIndexStorage);
		oo.setOptimizeUpdateManager(oum);

		oo.init();


		 cim = new ConcurrentIndexManager();
		IndexManagementTimerTask indexer = new IndexManagementTimerTask();
		IndexManagementTimerTask merger = new IndexManagementTimerTask();
		IndexManagementTimerTask optimizer = new IndexManagementTimerTask();

		MockComponentManager componentManager = new MockComponentManager();
		MockEventTrackingService eventTrackingService = new MockEventTrackingService();
		MockSearchService searchService = new MockSearchService();
		searchService.setDatasource(tds.getDataSource());
		MockSessionManager sessionManager = new MockSessionManager();
		MockUserDirectoryService userDirectoryService = new MockUserDirectoryService();

		ConcurrentSearchIndexBuilderWorkerImpl csibw = new ConcurrentSearchIndexBuilderWorkerImpl();
		csibw.setComponentManager(componentManager);
		csibw.setServerConfigurationService(serverConfigurationService);
		csibw.setEventTrackingService(eventTrackingService);
		csibw.setIndexWorker(tiw);
		csibw.setSearchService(searchService);
		csibw.setSessionManager(sessionManager);
		csibw.setSoakTest(true);
		csibw.setUserDirectoryService(userDirectoryService);
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
		docloader.setDelay(5);
		docloader.setPeriod(500);
		docloader.setManagementOperation(new ManagementOperation() {

			public void runOnce()
			{
				try
				{
					tds.populateDocuments(500,instanceName);
				}
				catch (Exception e)
				{
					log.error("Failed to LoadDocuments ",e);
				}
			}
			
		});
		
		taskList.add(docloader);
		
		 
		journaledFSIndexStorage.addIndexListener(cim);
		
		// Journal Optimization
		
		JournalOptimizationOperation journalOptimizationOperation = new JournalOptimizationOperation();
		JournalOptimizationManagerImpl journalOptimizationManager = new JournalOptimizationManagerImpl();

		JournalOptimizationTransactionListener journalOptimizationTransactionListener = new JournalOptimizationTransactionListener();
		SharedFilesystemLoadTransactionListener sharedFilesystemLoadTransactionListener = new SharedFilesystemLoadTransactionListener();
		SharedFilesystemSaveTransactionListener sharedFilesystemSaveTransactionListener = new SharedFilesystemSaveTransactionListener();
		OptimizeSharedTransactionListenerImpl optimizeSharedTransactionListener = new OptimizeSharedTransactionListenerImpl();

		DbJournalOptimizationManager optimizeJournalManager = new DbJournalOptimizationManager();
		TransactionSequenceImpl optimizeSequence = new TransactionSequenceImpl();
		sequence.getClass();
		MockClusterService clusterService = new MockClusterService();
		clusterService.addServerConfigurationService(serverConfigurationService);


		long journalOptimizeLimit = 5;

		optimizeSequence.setName("journaloptimize");
		optimizeSequence.setDatasource(tds.getDataSource());

		optimizeJournalManager.setClusterService(clusterService);
		optimizeJournalManager.setDatasource(tds.getDataSource());
		optimizeJournalManager.setJournalOptimizeLimit(journalOptimizeLimit);
		optimizeJournalManager.setServerConfigurationService(serverConfigurationService);


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
		journalOptimizationManager.setJournalManager(optimizeJournalManager);
		journalOptimizationManager.setSequence(optimizeSequence);

		List<TransactionListener> tl = new ArrayList<TransactionListener>();
		tl.add(journalOptimizationTransactionListener);
		tl.add(sharedFilesystemLoadTransactionListener);
		tl.add(optimizeSharedTransactionListener);
		tl.add(sharedFilesystemSaveTransactionListener);
		journalOptimizationManager.setTransactionListeners(tl);

		journalOptimizationOperation
				.setJournalOptimizationManager(journalOptimizationManager);

		optimizeJournalManager.init();
		optimizeSequence.init();
		clusterService.init();
		journalOptimizationOperation.init();
		
		IndexManagementTimerTask journalOptimizer = new IndexManagementTimerTask();
		journalOptimizer.setManagementOperation(journalOptimizationOperation);
		journalOptimizer.setDelay(10);
		journalOptimizer.setPeriod(1000);
		taskList.add(journalOptimizer);

		
		
		
		
		cim.setTasks(taskList);		
		cim.init();
		

	}

	/**
	 * @throws Exception 
	 * 
	 */
	public void close() throws Exception
	{
		cim.close();
		tds.close();
		
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void testSearch() throws IOException
	{
		IndexSearcher is = journaledFSIndexStorage.getIndexSearcher();
		TermQuery tq = new TermQuery(new Term(SearchService.FIELD_CONTENTS,"node"));
		
		Hits h = is.search(tq);
		log.info("Got "+h.length()+" hits from "+is.getIndexReader().numDocs());
	}

}
