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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.journal.api.ManagementOperation;
import org.sakaiproject.search.journal.impl.ConcurrentIndexManager;
import org.sakaiproject.search.journal.impl.IndexManagementTimerTask;
import org.sakaiproject.search.mock.MockSearchService;
import org.sakaiproject.search.mock.MockThreadLocalManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb
 */
public class ConcurrentIndexManagerTest extends TestCase
{
	protected static final Log log = LogFactory.getLog(ConcurrentIndexManagerTest.class);

	private int nrun = 0;

	/**
	 * @param name
	 */
	public ConcurrentIndexManagerTest(String name)
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.journal.impl.ConcurrentIndexManager#init()}.
	 * 
	 * @throws InterruptedException
	 */
	public final void testInit() throws InterruptedException
	{
		ThreadLocalManager threadLocalManager = new MockThreadLocalManager();
		log.info("================================== "+this.getClass().getName()+".testInit");
		ConcurrentIndexManager cim = new ConcurrentIndexManager();
		List<IndexManagementTimerTask> limtt = new ArrayList<IndexManagementTimerTask>();
		for (int i = 1; i < 11; i++)
		{
			IndexManagementTimerTask imtt = new IndexManagementTimerTask();
			imtt.setThreadLocalManager(threadLocalManager);
			imtt.setManagementOperation(new ManagementOperation()
			{

				int runs = 0;

				public void runOnce()
				{
					if (runs < 2)
					{
						nrun++;
						runs++;
					}

				}

			});
			imtt.setDelay(i * 100);
			imtt.setPeriod(i * 20);
			limtt.add(imtt);
		}
		cim.setTasks(limtt);
		cim.setSearchService(new MockSearchService());
		cim.init();
		Thread.sleep(30 * 100);
		assertEquals("Not all tasks ran ", 20, nrun);
		cim.close();
		log.info("=PASSED=========================== "+this.getClass().getName()+".testInit");


	}

}
