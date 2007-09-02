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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import junit.framework.TestCase;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.cluster.impl.ClusterDbJournal;
import org.sakaiproject.search.cluster.impl.ClusterSharedFilesystem;
import org.sakaiproject.search.index.impl.StandardAnalyzerFactory;
import org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener;
import org.sakaiproject.search.indexer.api.IndexWorkerListener;
import org.sakaiproject.search.indexer.debug.DebugIndexWorkerDocumentListener;
import org.sakaiproject.search.indexer.debug.DebugIndexWorkerListener;
import org.sakaiproject.search.indexer.debug.DebugTransactionListener;
import org.sakaiproject.search.indexer.impl.SearchBuilderQueueManager;
import org.sakaiproject.search.indexer.impl.TransactionIndexManagerImpl;
import org.sakaiproject.search.indexer.impl.TransactionSequenceImpl;
import org.sakaiproject.search.indexer.impl.TransactionalIndexWorker;
import org.sakaiproject.search.mock.MockSearchIndexBuilder;
import org.sakaiproject.search.mock.MockServerConfigurationService;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.util.FileUtils;

/**
 * @author ieb
 */
public class TransactionalIndexWorkerTest extends TestCase
{

	private SearchIndexBuilder mockSearchIndexBuilder;

	private ServerConfigurationService mockServerConfigurationService;

	private TransactionIndexManagerImpl transactionIndexManager;

	private TestDataSource tds;

	private SearchBuilderQueueManager searchBuilderQueueManager;

	private File testBase;

	private TransactionalIndexWorker tiw;

	private File work;

	private File shared;

	private File work2;

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
		tds = new TestDataSource();
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

		ClusterSharedFilesystem sharedFileSystem = new ClusterSharedFilesystem();
		sharedFileSystem.setJournalLocation(shared.getAbsolutePath());
		
		ClusterDbJournal journal = new ClusterDbJournal();
		journal.setDatasource(tds.getDataSource());

		transactionIndexManager = new TransactionIndexManagerImpl();
		transactionIndexManager.setAnalyzerFactory(new StandardAnalyzerFactory());
		transactionIndexManager.setSearchIndexWorkingDirectory(work.getAbsolutePath());
		transactionIndexManager.setSequence(sequence);

		searchBuilderQueueManager = new SearchBuilderQueueManager();
		searchBuilderQueueManager.setDatasource(tds.getDataSource());
		searchBuilderQueueManager.setSearchIndexBuilder(mockSearchIndexBuilder);

		transactionIndexManager.addTransactionListener(searchBuilderQueueManager);
		transactionIndexManager.addTransactionListener(sharedFileSystem);
		transactionIndexManager.addTransactionListener(journal);
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

		testInit();
		assertEquals("Should not have processed any documents ", 0, tiw.process(100));
		FileUtils.listDirectory(testBase);
	}

	public final void testProcessSome() throws Exception
	{
		testInit();
		int n = populateDocuments();
		assertEquals("Should not have processed some documents ", n, tiw.process(100));
		FileUtils.listDirectory(testBase);
		for (int i = 0; i < 100; i++)
		{
			File zipFile = new File(shared, String.valueOf(i) + ".zip");
			if (zipFile.exists())
			{
				FileInputStream fin = new FileInputStream(zipFile);
				FileUtils.unpack(fin, work2);
				FileUtils.listDirectory(work2);
			}
		}
	}

	/**
	 * @throws SQLException 
	 */
	private int populateDocuments() throws SQLException
	{
		int nitems = 0;
		Connection connection = null;
		PreparedStatement insertPST = null;
		try
		{
			connection = tds.getDataSource().getConnection();
			insertPST = connection
					.prepareStatement("insert into searchbuilderitem "
							+ "(id,version,name,context,searchaction,searchstate,itemscope) values "
							+ "(?,?,?,?,?,?,?)");
			for (int i = 0; i < 100; i++)
			{
				int state = i % SearchBuilderItem.states.length;
				String name = SearchBuilderItem.states[state];
				int action = i % 3;
				if (state == SearchBuilderItem.STATE_PENDING
						&& action == SearchBuilderItem.ACTION_ADD)
				{
					nitems++;
				}
				insertPST.clearParameters();
				insertPST.setString(1, String.valueOf(System.currentTimeMillis())
						+ String.valueOf(i));
				insertPST.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
				insertPST.setString(3, "/" + name + "/at/a/location/" + i);
				insertPST.setString(4, "/" + name + "/at/a");
				insertPST.setInt(5, action);
				insertPST.setInt(6, state);
				insertPST.setInt(7, SearchBuilderItem.ITEM);
				insertPST.execute();
			}
			connection.commit();
		}
		finally
		{
			try
			{
				insertPST.close();
			}
			catch (Exception ex2)
			{
			}
			try
			{
				connection.close();
			}
			catch (Exception ex2)
			{
			}
		}
		return nitems;

	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.indexer.impl.TransactionalIndexWorker#addIndexWorkerListener(org.sakaiproject.search.indexer.api.IndexWorkerListener)}.
	 */
	public final void testAddIndexWorkerListener()
	{
		testInit();
		tiw.addIndexWorkerListener(new DebugIndexWorkerListener());
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.indexer.impl.TransactionalIndexWorker#removeIndexWorkerListener(org.sakaiproject.search.indexer.api.IndexWorkerListener)}.
	 */
	public final void testRemoveIndexWorkerListener()
	{
		testInit();
		IndexWorkerListener iwdl = new DebugIndexWorkerListener();
		tiw.addIndexWorkerListener(iwdl);
		tiw.removeIndexWorkerListener(iwdl);
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.indexer.impl.TransactionalIndexWorker#addIndexWorkerDocumentListener(org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener)}.
	 */
	public final void testAddIndexWorkerDocumentListener()
	{
		testInit();
		tiw.addIndexWorkerDocumentListener(new DebugIndexWorkerDocumentListener());
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.indexer.impl.TransactionalIndexWorker#removeIndexWorkerDocumentListener(org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener)}.
	 */
	public final void testRemoveIndexWorkerDocumentListener()
	{
		testInit();
		IndexWorkerDocumentListener iwdl = new DebugIndexWorkerDocumentListener();
		tiw.addIndexWorkerDocumentListener(iwdl);
		tiw.removeIndexWorkerDocumentListener(iwdl);
	}

}
