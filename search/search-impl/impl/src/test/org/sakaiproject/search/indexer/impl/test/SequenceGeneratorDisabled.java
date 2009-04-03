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
public class SequenceGeneratorDisabled extends TestCase
{

	protected static final Log log = LogFactory.getLog(SequenceGeneratorDisabled.class);

	private TransactionSequenceImpl sequenceGenerator;

	private ConcurrentHashMap<Long, Long> m = new ConcurrentHashMap<Long, Long>();

	private int nt;

	protected long fail;

	private TDataSource tds;

	private long last;

	/**
	 * @param name
	 */
	public SequenceGeneratorDisabled(String name)
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

		tds = new TDataSource(30,false);
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
		
		log.info("================================== "+this.getClass().getName()+".testGetNextId");
		nt = 0;
		fail = 0;
		final ConcurrentHashMap<Long, Long> m = new ConcurrentHashMap<Long, Long>();
		for (int i = 0; i < 20; i++)
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
		log.info("==PASSED========================== "+this.getClass().getName()+".testGetNextId");

	}
	/**
	 * Test method for
	 * {@link org.sakaiproject.search.transaction.impl.TransactionSequenceImpl#getNextId()}.
	 */
	public final void testGetNextIdWrapped()
	{
		sequenceGenerator.setMinValue(10);
		sequenceGenerator.setMaxValue(200);
		log.info("================================== "+this.getClass().getName()+".testGetNextId");
		nt = 0;
		fail = 0;
		last = 0;
		for (int i = 0; i < 20; i++)
		{
			Thread t = new Thread(new Runnable()
			{

				public void run()
				{
					try
					{
						nt++;
						long n = 0;
						long locallast = 0;
						Map<Long, Long> seq = new HashMap<Long, Long>();
						for (int i = 0; i < 1000; i++)
						{
							n = sequenceGenerator.getNextId();
							if ( n < last ) {
								log.debug("Wrapped "+last+":"+n);
								 m = new ConcurrentHashMap<Long, Long>();
							}
							if ( n < locallast ) {
								log.debug("Wrapped Local"+locallast+":"+n);
								seq = new HashMap<Long, Long>();
							}
							last = n;
							locallast = n;
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
		log.info("==PASSED========================== "+this.getClass().getName()+".testGetNextId");

	}
}
