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

package org.sakaiproject.search.index.soaktest;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.mock.MockClusterService;
import org.sakaiproject.search.util.FileUtils;

/**
 * to run this test use mvn -Dtest=SearchSoak test
 * 
 * @author ieb
 */
public class SearchSoak extends TestCase
{

	private static final Log log = LogFactory.getLog(SearchSoak.class);

	private File testBase;

	private File dbFile;

	public SearchSoak(String name)
	{
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		// TODO Auto-generated method stub
		super.setUp();
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		// TODO Auto-generated method stub
		super.tearDown();
	}

	/**
	 * @throws Exception
	 */
	private void init() throws Exception
	{
		testBase = new File("target");
		testBase = new File(testBase, "SearchSoak");
		dbFile = new File(testBase, "searchsoakdb");
		FileUtils.deleteAll(testBase);
	}

	/*
	 * public void testSoakOneNode() throws Exception { SearchIndexerNode node =
	 * new SearchIndexerNode(testBase.getAbsolutePath(),"onethread");
	 * node.init(); Thread.sleep(30 * 1000); }
	 */
	public void testSoakTenCLuster() throws Exception
	{
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		String url = "jdbc:derby:" + testBase.getAbsolutePath() + ";create=true";
		String user = "sa";
		String password = "manager";
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			driver = "com.mysql.jdbc.Driver";
			url = "dbc:mysql://127.0.0.1:3306/sakai22?useUnicode=true&characterEncoding=UTF-8";
			user = "sakai22";
			password = "sakai22";
		}
		catch (ClassNotFoundException cnfe)
		{
			log.debug(cnfe);
		}

		log.info("Using " + driver);
		SearchIndexerNode[] node = new SearchIndexerNode[4];
		MockClusterService clusterService = new MockClusterService();
		for (int i = 0; i < 4; i++)
		{
			node[i] = new SearchIndexerNode(clusterService, testBase.getAbsolutePath(),
					"node" + i, driver, url, user, password);
			node[i].init();
		}
		clusterService.init();

		long endTime = System.currentTimeMillis() + 3600000;
		long timeLeft = endTime - System.currentTimeMillis();
		while (timeLeft > 0)
		{
			for (int i = 0; i < 4; i++)
			{
				node[i].testSearch();
				// simulate holes in the sequence
				node[i].makeTransactionHole();
			}
			Thread.sleep(1000);
			for (int i = 0; i < 4; i++)
			{
				node[i].getThreadLocalManager().clear();
			}

			timeLeft = endTime - System.currentTimeMillis();
		}

		for (int i = 0; i < 4; i++)
		{
			node[i].close();
		}
		for (int i = 0; i < 4; i++)
		{
			node[i].cleanup();
		}
		log.info(" waiting for 30 ");
		Thread.sleep(30000);
		Runtime.getRuntime().gc();
		for (int k = 0; k < 50; k++)
		{
			for (int i = 0; i < 4; i++)
			{
				node[i].testSearch();
			}
			log.info("Done " + k);
		}
		log.info(" take snapshot will terminate in 1h, ctrl+C to exit ");
		Thread.sleep(3600000);

	}

}
