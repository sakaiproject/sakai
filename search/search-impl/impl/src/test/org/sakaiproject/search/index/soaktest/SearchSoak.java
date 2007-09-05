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

import java.io.File;

import junit.framework.TestCase;

import org.sakaiproject.search.util.FileUtils;

/**
 * to run this test use mvn -Dtest=SearchSoak test
 * @author ieb
 *
 */
public class SearchSoak extends TestCase
{

	private File testBase;
	private File dbFile;

	public SearchSoak(String name) {
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
		testBase = new File("m2-target");
		testBase = new File(testBase, "SearchSoak");
		dbFile = new File(testBase, "searchsoakdb");
		FileUtils.deleteAll(testBase);
	}

	
/*	public void testSoakOneNode() throws Exception {
		SearchIndexerNode node = new SearchIndexerNode(testBase.getAbsolutePath(),"onethread");
		node.init();
		Thread.sleep(30 * 1000);
	}
	*/
	public void testSoakTenCLuster() throws Exception {
		for ( int i = 0; i < 10; i++ ) {
			SearchIndexerNode node = new SearchIndexerNode(testBase.getAbsolutePath(),"node"+i,
					"org.hsqldb.jdbcDriver",
					"jdbc:hsqldb:file:"+dbFile.getAbsolutePath(),
					"sa","");
			node.init();
		}
		Thread.sleep(600*1000);
	}

}
