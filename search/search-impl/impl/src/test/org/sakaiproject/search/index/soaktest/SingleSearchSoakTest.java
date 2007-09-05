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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.util.FileUtils;

/**
 * to run this test use mvn -Dtest=SearchSoak test
 * @author ieb
 *
 */
public class SingleSearchSoakTest extends TestCase
{

	private static final Log log = LogFactory.getLog(SingleSearchSoakTest.class);
	private File testBase;

	public SingleSearchSoakTest(String name) {
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
		testBase = new File(testBase, "SingleSearchSoak");
		FileUtils.deleteAll(testBase);
	}

	
	public void testSoakOneNode() throws Exception {
		log.info("================================== "+this.getClass().getName()+".testSoakOneNode");
		SearchIndexerNode node = new SearchIndexerNode(testBase.getAbsolutePath(),"onethread");
		node.init();
		Thread.sleep(15 * 1000);
		node.close();
		log.info("=PASSED=========================== "+this.getClass().getName()+".testSoakOneNode");
	}

}
