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
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.search.index.AnalyzerFactory;
import org.sakaiproject.search.index.impl.StandardAnalyzerFactory;
import org.sakaiproject.search.journal.impl.SharedFilesystemJournalStorage;
import org.sakaiproject.search.mock.MockClusterService;
import org.sakaiproject.search.mock.MockServerConfigurationService;
import org.sakaiproject.search.optimize.shared.impl.DbJournalOptimizationManager;
import org.sakaiproject.search.optimize.shared.impl.JournalOptimizationManagerImpl;
import org.sakaiproject.search.optimize.shared.impl.JournalOptimizationOperation;
import org.sakaiproject.search.optimize.shared.impl.JournalOptimizationTransactionListener;
import org.sakaiproject.search.optimize.shared.impl.OptimizeSharedTransactionListenerImpl;
import org.sakaiproject.search.optimize.shared.impl.SharedFilesystemLoadTransactionListener;
import org.sakaiproject.search.optimize.shared.impl.SharedFilesystemSaveTransactionListener;
import org.sakaiproject.search.transaction.api.TransactionListener;
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
		shared = new File(testBase, "shared");
		index = new File(testBase, "index");

		FileUtils.deleteAll(testBase);

		tds = new TDataSource(5, false);

		journalOptimizationOperation = new JournalOptimizationOperation();
		JournalOptimizationManagerImpl journalOptimizationManager = new JournalOptimizationManagerImpl();

		JournalOptimizationTransactionListener journalOptimizationTransactionListener = new JournalOptimizationTransactionListener();
		SharedFilesystemLoadTransactionListener sharedFilesystemLoadTransactionListener = new SharedFilesystemLoadTransactionListener();
		SharedFilesystemSaveTransactionListener sharedFilesystemSaveTransactionListener = new SharedFilesystemSaveTransactionListener();
		OptimizeSharedTransactionListenerImpl optimizeSharedTransactionListener = new OptimizeSharedTransactionListenerImpl();

		SharedFilesystemJournalStorage sharedFilesystemJournalStorage = new SharedFilesystemJournalStorage();
		DbJournalOptimizationManager journalManager = new DbJournalOptimizationManager();
		TransactionSequenceImpl sequence = new TransactionSequenceImpl();
		AnalyzerFactory analyzerFactory = new StandardAnalyzerFactory();

		ServerConfigurationService serverConfigurationService = new MockServerConfigurationService();
		MockClusterService clusterService = new MockClusterService();
		clusterService.addServerConfigurationService(serverConfigurationService);

		String journalLocation = shared.getAbsolutePath();

		long journalOptimizeLimit = 5;

		sequence.setName("journaloptimize");
		sequence.setDatasource(tds.getDataSource());

		journalManager.setClusterService(clusterService);
		journalManager.setDatasource(tds.getDataSource());
		journalManager.setJournalOptimizeLimit(journalOptimizeLimit);
		journalManager.setServerConfigurationService(serverConfigurationService);

		sharedFilesystemJournalStorage.setJournalLocation(journalLocation);

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
		journalOptimizationManager.setJournalManager(journalManager);
		journalOptimizationManager.setSequence(sequence);

		List<TransactionListener> tl = new ArrayList<TransactionListener>();
		tl.add(journalOptimizationTransactionListener);
		tl.add(sharedFilesystemLoadTransactionListener);
		tl.add(optimizeSharedTransactionListener);
		tl.add(sharedFilesystemSaveTransactionListener);
		journalOptimizationManager.setTransactionListeners(tl);

		journalOptimizationOperation
				.setJournalOptimizationManager(journalOptimizationManager);

		journalManager.init();
		sequence.init();
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
	 */
	public final void testRunOnce()
	{
		log.info("================================== " + this.getClass().getName()
				+ ".testRunOnce");
		journalOptimizationOperation.init();
		journalOptimizationOperation.runOnce();
		log.info("=PASSED=========================== " + this.getClass().getName()
				+ ".testRunOnce");
	}

}
