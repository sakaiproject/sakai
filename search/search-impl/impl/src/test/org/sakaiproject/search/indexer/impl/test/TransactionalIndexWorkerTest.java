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
import java.io.FileInputStream;
import java.io.IOException;

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
import org.sakaiproject.search.journal.impl.SharedFilesystemJournalStorage;
import org.sakaiproject.search.mock.MockSearchIndexBuilder;
import org.sakaiproject.search.mock.MockServerConfigurationService;
import org.sakaiproject.search.transaction.impl.TransactionSequenceImpl;
import org.sakaiproject.search.util.FileUtils;

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

	private File work;

	private File shared;

	private File work2;

	private JournalManagerUpdateTransaction journalManagerUpdateTransaction;

	private SharedFilesystemJournalStorage sharedFilesystemJournalStorage;

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
		testBase = new File("m2-target");
		testBase = new File(testBase, "TransactionalIndexWorkerTest");
		work = new File(testBase, "work");
		shared = new File(testBase, "shared");
		work2 = new File(testBase, "work2");

		mockSearchIndexBuilder = new MockSearchIndexBuilder();
		mockServerConfigurationService = new MockServerConfigurationService();
		TransactionSequenceImpl sequence = new TransactionSequenceImpl();
		sequence.setDatasource(tds.getDataSource());
		sequence.setName("TransactionalIndexWorkerTest");

		SharedFilesystemJournalStorage sharedFileSystem = new SharedFilesystemJournalStorage();
		sharedFileSystem.setJournalLocation(shared.getAbsolutePath());
		
		DbJournalManager journalManager = new DbJournalManager();
		journalManager.setDatasource(tds.getDataSource());
		
		

		transactionIndexManager = new TransactionIndexManagerImpl();
		transactionIndexManager.setAnalyzerFactory(new StandardAnalyzerFactory());
		transactionIndexManager.setSearchIndexWorkingDirectory(work.getAbsolutePath());
		transactionIndexManager.setSequence(sequence);

		journalManagerUpdateTransaction = new JournalManagerUpdateTransaction();
		journalManagerUpdateTransaction.setJournalManager(journalManager);
		
		JournalStorageUpdateTransactionListener journalStorageUpdateTransactionListener = new JournalStorageUpdateTransactionListener();
		journalStorageUpdateTransactionListener.setJournalStorage(sharedFileSystem);
		
		searchBuilderQueueManager = new SearchBuilderQueueManager();
		searchBuilderQueueManager.setDatasource(tds.getDataSource());
		searchBuilderQueueManager.setSearchIndexBuilder(mockSearchIndexBuilder);

		transactionIndexManager.addTransactionListener(searchBuilderQueueManager);
		transactionIndexManager.addTransactionListener(journalStorageUpdateTransactionListener);
		transactionIndexManager.addTransactionListener(journalManagerUpdateTransaction);
		transactionIndexManager.addTransactionListener(new DebugTransactionListener());

		sequence.init();
		searchBuilderQueueManager.init();
		transactionIndexManager.init();

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
		int n = tds.populateDocuments(100);
		assertEquals("Should not have processed some documents ", n, tiw.process(100));
		for (int i = 0; i < 100; i++)
		{
			File zipFile = new File(shared, String.valueOf(i) + ".zip");
			if (zipFile.exists())
			{
				FileInputStream fin = new FileInputStream(zipFile);
				FileUtils.unpack(fin, work2);
			}
		}
		FileUtils.listDirectory(work2);
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
