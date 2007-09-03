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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.transaction.impl.TransactionSequenceImpl;

/**
 * @author ieb
 */
public class SequenceGeneratorTest extends TestCase
{

	protected static final Log log = LogFactory.getLog(SequenceGeneratorTest.class);

	private TransactionSequenceImpl sequenceGenerator;


	private int nt;

	protected long fail;

	private TestDataSource tds;

	/**
	 * @param name
	 */
	public SequenceGeneratorTest(String name)
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
		sequenceGenerator = new TransactionSequenceImpl();
		sequenceGenerator.setDatasource(tds.getDataSource());
		sequenceGenerator.setName("testsequeence");
		sequenceGenerator.init();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		tds.close();
		super.tearDown();
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.transaction.impl.TransactionSequenceImpl#getNextId()}.
	 */
	public final void testGetNextId()
	{
		nt = 0;
		fail = 0;
		final ConcurrentHashMap<Long, Long> m = new ConcurrentHashMap<Long, Long>();
		for (int i = 0; i < 50; i++)
		{
			Thread t = new Thread(new Runnable()
			{

				public void run()
				{
					try
					{
						nt++;
						long n = 0;
						Map<Long, Long> seq = new HashMap<Long, Long>();
						for (int i = 0; i < 1000; i++)
						{
							n = sequenceGenerator.getNextId();
							if (seq.get(n) != null)
							{
								fail = n;
								fail("Local clash on " + n);

							}
							seq.put(n, n);
						}
						log.debug("Last " + n);
						for (long nx : seq.values())
						{
							if (m.get(nx) != null)
							{
								fail = nx;
								fail("Concurrent clash on " + nx);
							}
							m.put(nx, nx);
						}
					}
					finally
					{
						nt--;
					}

				}

			});
			t.start();
		}
		while (nt > 0)
		{
			if (fail != 0)
			{
				fail("Failed with clash on " + fail);
			}
			Thread.yield();
		}
		log.info("testGetNextId passed");

	}
}
