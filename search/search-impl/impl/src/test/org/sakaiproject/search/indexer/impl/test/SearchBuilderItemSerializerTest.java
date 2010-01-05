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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.indexer.impl.SearchBuilderItemSerializer;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.model.impl.SearchBuilderItemImpl;
import org.sakaiproject.search.util.FileUtils;

/**
 * @author ieb
 */
public class SearchBuilderItemSerializerTest extends TestCase
{

	private static final Log log = LogFactory
			.getLog(SearchBuilderItemSerializerTest.class);

	private File testBase;

	private File work;

	private SearchBuilderItemSerializer sbis = new SearchBuilderItemSerializer();

	/**
	 * @param name
	 */
	public SearchBuilderItemSerializerTest(String name)
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
		testBase = new File("target");
		testBase = new File(testBase, "SearchBuilderItemSerializerTest");
		work = new File(testBase, "work");
		if (!work.mkdirs()) {
			log.warn("Couldn't creatre directory " + work.getPath());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
		FileUtils.deleteAll(testBase);
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.indexer.impl.SearchBuilderItemSerializer#saveTransactionList(java.io.File, java.util.List)}.
	 * 
	 * @throws IOException
	 */
	public final void testSaveTransactionList() throws IOException
	{
		log.info("================================== "+this.getClass().getName()+".testSaveTransactionList");
		List<SearchBuilderItem> testList = newSearchBuilderItemList();
		File f = new File(work, "testSaveTransactionList");
		if (!f.mkdirs()) {
			log.warn("Could not create directory " + f.getPath());
		}
		sbis.saveTransactionList(f, testList);
		log.info("==PASSED========================== "+this.getClass().getName()+".testSaveTransactionList");

	}

	/**
	 * @return
	 */
	private List<SearchBuilderItem> newSearchBuilderItemList()
	{
		List<SearchBuilderItem> lsbi = new ArrayList<SearchBuilderItem>();
		for (int i = 0; i < 100; i++)
		{
			SearchBuilderItem sbi = new SearchBuilderItemImpl();
			sbi.setContext(String.valueOf("Context" + UUID.randomUUID().toString()));
			sbi.setId(String.valueOf(UUID.randomUUID().toString()));
			sbi.setItemscope(SearchBuilderItem.ITEM);
			sbi.setName("Name" + String.valueOf(UUID.randomUUID().toString()));
			sbi.setSearchaction(i % SearchBuilderItem.actions.length);
			sbi.setSearchstate(i % SearchBuilderItem.states.length);
			sbi.setVersion(new Date(System.currentTimeMillis() + i + 2000));
			lsbi.add(sbi);
		}
		return lsbi;
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.indexer.impl.SearchBuilderItemSerializer#loadTransactionList(java.io.File)}.
	 * 
	 * @throws IOException
	 */
	public final void testLoadTransactionList() throws IOException
	{
		log.info("================================== "+this.getClass().getName()+".testLoadTransactionList");
		List<SearchBuilderItem> testList = newSearchBuilderItemList();
		File f = new File(work, "testLoadTransactionList");
		if (!f.mkdirs()) 
		{
			log.warn("Could not create directory " + f.getPath());
		}
		sbis.saveTransactionList(f, testList);
		List<SearchBuilderItem> loadedList = sbis.loadTransactionList(f);
		assertEquals("Size of locaded list not the same", testList.size(), loadedList
				.size());
		for (int i = 0; i < testList.size(); i++)
		{
			SearchBuilderItem sbit = testList.get(i);
			SearchBuilderItem sbil = loadedList.get(i);
			assertEquals("Contexts dont match ", sbit.getContext(), sbil.getContext());
			assertEquals("Id dont match ", sbit.getId(), sbil.getId());
			assertEquals("Itemscope dont match ", sbit.getItemscope(), sbil
					.getItemscope());
			assertEquals("Name dont match ", sbit.getName(), sbil.getName());
			assertEquals("Search Action dont match ", sbit.getSearchaction(), sbil
					.getSearchaction());
			assertEquals("Search State dont match ", sbit.getSearchstate(), sbil
					.getSearchstate());
			assertEquals("Version dont match ", sbit.getVersion(), sbil.getVersion());
		}
		log.info("==PASSED========================== "+this.getClass().getName()+".testLoadTransactionList");
	}

}
