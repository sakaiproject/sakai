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
 * @author ieb
 *
 */
public class OpenFilesTest extends TestCase
{

	private static final Log log = LogFactory.getLog(OpenFilesTest.class);
	private File testBase;

	public OpenFilesTest(String name) {
		super(name);
	}
	
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		// TODO Auto-generated method stub
		super.setUp();
		init();
	}
	
	/* (non-Javadoc)
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
	 * 
	 */
	private void init() throws Exception
	{
		testBase = new File("target");
		testBase = new File(testBase, "OpenFiles");
		FileUtils.deleteAll(testBase);
	}

	
	public void testOpenFiles() throws Exception {
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		String url = "jdbc:derby:" + testBase.getAbsolutePath() + ";create=true";
		String user = "sa";
		String password = "manager";

		log.info("================================== "+this.getClass().getName()+".testSoakOneNode");
		MockClusterService clusterService = new MockClusterService();
		SearchIndexerNode node = new SearchIndexerNode(clusterService,testBase.getAbsolutePath(),
				"onethread",driver, url, user,password);
		node.init();
		clusterService.init();
		Thread.sleep(10000);
		// to test for real problems, change the 10 to 1000 and run in eclipse
		for ( int k = 0; k < 10; k++ ) {
			node.reopenIndex();
			Thread.sleep(500);
			node.testSlowSearch();
			node.getThreadLocalManager().clear();
		}
		node.close();
		Thread.sleep(1000);
		log.info("=PASSED=========================== "+this.getClass().getName()+".testSoakOneNode");
	}

}
