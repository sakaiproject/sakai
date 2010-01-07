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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.journal.api.JournalExhausetedException;
import org.sakaiproject.search.journal.api.JournalManagerState;
import org.sakaiproject.search.journal.impl.DbJournalManager;
import org.sakaiproject.search.mock.MockServerConfigurationService;

/**
 * @author ieb
 *
 */
public class DbJournalManagerTest extends TestCase
{

	private static final Log log = LogFactory.getLog(DbJournalManagerTest.class);
	private TDataSource tds;

	/**
	 * @param name
	 */
	public DbJournalManagerTest(String name)
	{
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
		tds = new TDataSource(10,false);

	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
		tds.close();
	}

	/**
	 * Test method for {@link org.sakaiproject.search.journal.impl.DbJournalManager#getNextSavePoint(long)}.
	 * @throws Exception 
	 */
	public final void testGetNextSavePoint() throws Exception
	{
		log.info("================================== "+this.getClass().getName()+".testGetNextSavePoint");

		DbJournalManager dbJournalManager = new DbJournalManager();
		dbJournalManager.setServerConfigurationService(new MockServerConfigurationService());
		dbJournalManager.setDatasource(tds.getDataSource());
		dbJournalManager.init();
		for ( int i = 0; i < 10; i++ ) {
			JournalManagerState jmstate = dbJournalManager.prepareSave(i);
			dbJournalManager.commitSave(jmstate);
		}
		for ( int i = 0; i < 9; i++ ) {
			assertEquals("Incorrect Journal Number returned",i+1, dbJournalManager.getNextSavePoint(i));
		}
		try {
			dbJournalManager.getNextSavePoint(9);
			fail("Should have exhausted the journal");
		} catch ( JournalExhausetedException jex ) {
			log.debug(jex);

		}
		log.info("==PASSED========================== "+this.getClass().getName()+".testGetNextSavePoint");
	}

	/**
	 * Test method for {@link org.sakaiproject.search.journal.impl.DbJournalManager#prepareSave(long)}.
	 * @throws Exception 
	 */
	public final void testPrepareSave() throws Exception
	{
		log.info("================================== "+this.getClass().getName()+".testPrepareSave");
		DbJournalManager dbJournalManager = new DbJournalManager();
		dbJournalManager.setServerConfigurationService(new MockServerConfigurationService());
		dbJournalManager.setDatasource(tds.getDataSource());
		dbJournalManager.init();
		JournalManagerState[] jms = new JournalManagerState[10];
		for ( int i = 0; i < 10; i++ ) {
			jms[i] = dbJournalManager.prepareSave(i);
			assertNotNull("Journal State was null ",jms);
		}
		for ( int i = 0; i < 10; i++ ) {
			dbJournalManager.rollbackSave(jms[i]);
		}
		log.info("==PASSED========================== "+this.getClass().getName()+".testPrepareSave");
	}

	/**
	 * Test method for {@link org.sakaiproject.search.journal.impl.DbJournalManager#commitSave(org.sakaiproject.search.journal.api.JournalManagerState)}.
	 * @throws Exception 
	 */
	public final void testCommitSave() throws Exception
	{
		log.info("================================== "+this.getClass().getName()+".testCommitSave");
		DbJournalManager dbJournalManager = new DbJournalManager();
		dbJournalManager.setServerConfigurationService(new MockServerConfigurationService());
		dbJournalManager.setDatasource(tds.getDataSource());
		dbJournalManager.init();
		for ( int i = 0; i < 10; i++ ) {
			JournalManagerState jmstate = dbJournalManager.prepareSave(i);
			dbJournalManager.commitSave(jmstate);
		}
		for ( int i = 0; i < 9; i++ ) {
			assertEquals("Incorrect Journal Number returned",i+1, dbJournalManager.getNextSavePoint(i));
		}
		try {
			dbJournalManager.getNextSavePoint(9);
			fail("Should have exhausted the journal");
		} catch ( JournalExhausetedException jex ) {
			log.debug(jex);
			
		}
		log.info("==PASSED========================== "+this.getClass().getName()+".testCommitSave");
	}

	/**
	 * Test method for {@link org.sakaiproject.search.journal.impl.DbJournalManager#rollbackSave(org.sakaiproject.search.journal.api.JournalManagerState)}.
	 * @throws Exception 
	 */
	public final void testRollbackSave() throws Exception
	{
		log.info("================================== "+this.getClass().getName()+".testRollbackSave");
		DbJournalManager dbJournalManager = new DbJournalManager();
		dbJournalManager.setServerConfigurationService(new MockServerConfigurationService());
		dbJournalManager.setDatasource(tds.getDataSource());
		dbJournalManager.init();
		for ( int i = 0; i < 100; i++ ) {
			JournalManagerState jmstate = dbJournalManager.prepareSave(i);
			dbJournalManager.rollbackSave(jmstate);
		}
		try {
			long i = dbJournalManager.getNextSavePoint(20);
			fail("Should have exhausted the journal got "+i);
		} catch ( JournalExhausetedException jex ) {
			log.debug(jex);

		}
		log.info("==PASSED========================== "+this.getClass().getName()+".testRollbackSave");

	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void listAll() throws Exception
	{
		Connection connection = tds.getDataSource().getConnection();
		Statement s = connection.createStatement();
		ResultSet rs = s.executeQuery("select txid, txts,indexwriter from search_journal");
		log.info("Record ++++++++++");
		while (rs.next())
		{
			log.info("Record:" + rs.getString(1) + ":" + rs.getLong(2)+":"+rs.getString(3));
		}
		log.info("Record ----------");
		s.close();
		s.close();
		rs.close();
		connection.close();
	}

}
