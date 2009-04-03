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
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.index.impl.StandardAnalyzerFactory;
import org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener;
import org.sakaiproject.search.indexer.api.IndexWorkerListener;
import org.sakaiproject.search.indexer.debug.DebugIndexWorkerDocumentListener;
import org.sakaiproject.search.indexer.debug.DebugIndexWorkerListener;
import org.sakaiproject.search.indexer.debug.DebugTransactionListener;
import org.sakaiproject.search.indexer.impl.JournalManagerUpdateTransaction;
import org.sakaiproject.search.indexer.impl.JournalStorageUpdateTransactionListener;
import org.sakaiproject.search.indexer.impl.SearchBuilderQueueManager;
import org.sakaiproject.search.indexer.impl.TransactionIndexManagerImpl;
import org.sakaiproject.search.indexer.impl.TransactionalIndexWorker;
import org.sakaiproject.search.journal.impl.DbJournalManager;
import org.sakaiproject.search.journal.impl.JournalSettings;
import org.sakaiproject.search.journal.impl.SharedFilesystemJournalStorage;
import org.sakaiproject.search.mock.MockSearchIndexBuilder;
import org.sakaiproject.search.mock.MockServerConfigurationService;
import org.sakaiproject.search.mock.MockThreadLocalManager;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.transaction.impl.TransactionSequenceImpl;
import org.sakaiproject.search.util.FileUtils;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb
 */
public class TransactionalIndexWorkerTest extends TestCase
{

	private static final Log log = LogFactory.getLog(TransactionalIndexWorkerTest.class);

	private SearchIndexBuilder mockSearchIndexBuilder;

	private ServerConfigurationService mockServerConfigurationService;

	private TransactionIndexManagerImpl transactionIndexManager;

	private TDataSource tds;

	private SearchBuilderQueueManager searchBuilderQueueManager;

	private File testBase;

	private TransactionalIndexWorker tiw;

	private JournalManagerUpdateTransaction journalManagerUpdateTransaction;

	private SharedFilesystemJournalStorage sharedFilesystemJournalStorage;

	private ThreadLocalManager mockThreadLocalManager;

	/**
	 * @param name
	 */
	public TransactionalIndexWorkerTest(String name)
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
		tds = new TDataSource(5,false);
		testBase = new File("target");
		testBase = new File(testBase, "TransactionalIndexWorkerTest");
		
		String localIndexBase = new File(testBase,"local").getAbsolutePath();
		String sharedJournalBase = new File(testBase,"shared").getAbsolutePath();

		
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



		mockSearchIndexBuilder = new MockSearchIndexBuilder();
		mockServerConfigurationService = new MockServerConfigurationService();
		TransactionSequenceImpl sequence = new TransactionSequenceImpl();
		sequence.setDatasource(tds.getDataSource());
		sequence.setName("TransactionalIndexWorkerTest");

		SharedFilesystemJournalStorage sharedFileSystem = new SharedFilesystemJournalStorage();
		sharedFileSystem.setJournalSettings(journalSettings);
		
		MockServerConfigurationService serverConfigurationService = new MockServerConfigurationService();
		DbJournalManager journalManager = new DbJournalManager();
		journalManager.setDatasource(tds.getDataSource());
		journalManager.setServerConfigurationService(serverConfigurationService);
		
		

		transactionIndexManager = new TransactionIndexManagerImpl();
		transactionIndexManager.setAnalyzerFactory(new StandardAnalyzerFactory());
		transactionIndexManager.setJournalSettings(journalSettings);
		transactionIndexManager.setSequence(sequence);

		journalManagerUpdateTransaction = new JournalManagerUpdateTransaction();
		journalManagerUpdateTransaction.setJournalManager(journalManager);
		
		JournalStorageUpdateTransactionListener journalStorageUpdateTransactionListener = new JournalStorageUpdateTransactionListener();
		journalStorageUpdateTransactionListener.setJournalStorage(sharedFileSystem);
		
		searchBuilderQueueManager = new SearchBuilderQueueManager();
		
		TransactionSequenceImpl lockSequenceImpl = new TransactionSequenceImpl();
		lockSequenceImpl.setDatasource(tds.getDataSource());
		lockSequenceImpl.setName("queueManagerLock");
		lockSequenceImpl.setMinValue(2000);
		lockSequenceImpl.setMaxValue(10000000);
		lockSequenceImpl.init();
		searchBuilderQueueManager.setSequence(lockSequenceImpl);

		
		searchBuilderQueueManager.setDatasource(tds.getDataSource());
		searchBuilderQueueManager.setSearchIndexBuilder(mockSearchIndexBuilder);

		transactionIndexManager.addTransactionListener(searchBuilderQueueManager);
		transactionIndexManager.addTransactionListener(journalStorageUpdateTransactionListener);
		transactionIndexManager.addTransactionListener(journalManagerUpdateTransaction);
		transactionIndexManager.addTransactionListener(new DebugTransactionListener());

		sequence.init();
		searchBuilderQueueManager.init();
		transactionIndexManager.init();
		journalManager.init();
		
		
		mockThreadLocalManager = new MockThreadLocalManager();

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
		FileUtils.deleteAll(testBase);
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.indexer.impl.TransactionalIndexWorker#init()}.
	 */
	public final void testInit()
	{
		log.info("================================== "+this.getClass().getName()+".doInit");
		doInit();
		log.info("==PASSED========================== "+this.getClass().getName()+".doInit");

	}

	/**
	 * 
	 */
	private void doInit()
	{
		tiw = new TransactionalIndexWorker();
		tiw.setSearchIndexBuilder(mockSearchIndexBuilder);
		tiw.setServerConfigurationService(mockServerConfigurationService);
		tiw.setTransactionIndexManager(transactionIndexManager);
		tiw.setThreadLocalManager(mockThreadLocalManager);
		tiw.addIndexWorkerDocumentListener(new DebugIndexWorkerDocumentListener());
		tiw.addIndexWorkerListener(new DebugIndexWorkerListener());
		tiw.init();
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.indexer.impl.TransactionalIndexWorker#process()}.
	 * 
	 * @throws IOException
	 */
	public final void testProcessNone() throws IOException
	{

		log.info("================================== "+this.getClass().getName()+".testProcessNone");
		doInit();
		assertEquals("Should not have processed any documents ", 0, tiw.process(100));
		log.info("==PASSED========================== "+this.getClass().getName()+".testProcessNone");
	}

	public final void testProcessSome() throws Exception
	{
		log.info("================================== "+this.getClass().getName()+".testProcessSome");
		doInit();
		List<SearchBuilderItem> items = tds.populateDocuments(100,"testsome");
		int n = 0;
		
		for ( SearchBuilderItem sbi : items) {
			if (sbi.getSearchstate().equals(SearchBuilderItem.STATE_PENDING)
					&& (sbi.getSearchaction().equals(SearchBuilderItem.ACTION_ADD) ||
							sbi.getSearchaction().equals(SearchBuilderItem.ACTION_DELETE))	)
			{
				n++;
			}
		}
		
		assertEquals("Should have processed some documents ", n, tiw.process(100));
		log.info("==PASSED========================== "+this.getClass().getName()+".testProcessSome");
	}

	/**
	 * @throws SQLException 
	 */

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.indexer.impl.TransactionalIndexWorker#addIndexWorkerListener(org.sakaiproject.search.indexer.api.IndexWorkerListener)}.
	 */
	public final void testAddIndexWorkerListener()
	{
		log.info("================================== "+this.getClass().getName()+".testAddIndexWorkerListener");
		doInit();
		tiw.addIndexWorkerListener(new DebugIndexWorkerListener());
		log.info("==PASSED========================== "+this.getClass().getName()+".testAddIndexWorkerListener");
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.indexer.impl.TransactionalIndexWorker#removeIndexWorkerListener(org.sakaiproject.search.indexer.api.IndexWorkerListener)}.
	 */
	public final void testRemoveIndexWorkerListener()
	{
		log.info("================================== "+this.getClass().getName()+".testRemoveIndexWorkerListener");
		doInit();
		IndexWorkerListener iwdl = new DebugIndexWorkerListener();
		tiw.addIndexWorkerListener(iwdl);
		tiw.removeIndexWorkerListener(iwdl);
		log.info("==PASSED========================== "+this.getClass().getName()+".testRemoveIndexWorkerListener");
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.indexer.impl.TransactionalIndexWorker#addIndexWorkerDocumentListener(org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener)}.
	 */
	public final void testAddIndexWorkerDocumentListener()
	{
		log.info("================================== "+this.getClass().getName()+".testAddIndexWorkerDocumentListener");
		doInit();
		tiw.addIndexWorkerDocumentListener(new DebugIndexWorkerDocumentListener());
		log.info("==PASSED========================== "+this.getClass().getName()+".testAddIndexWorkerDocumentListener");
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.indexer.impl.TransactionalIndexWorker#removeIndexWorkerDocumentListener(org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener)}.
	 */
	public final void testRemoveIndexWorkerDocumentListener()
	{
		log.info("================================== "+this.getClass().getName()+".testRemoveIndexWorkerDocumentListener");
		doInit();
		IndexWorkerDocumentListener iwdl = new DebugIndexWorkerDocumentListener();
		tiw.addIndexWorkerDocumentListener(iwdl);
		tiw.removeIndexWorkerDocumentListener(iwdl);
		log.info("==PASSED========================== "+this.getClass().getName()+".testRemoveIndexWorkerDocumentListener");
	}

}
