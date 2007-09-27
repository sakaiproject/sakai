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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.search.journal.impl.JournaledFSIndexStorage;
import org.sakaiproject.search.mock.MockServerConfigurationService;
import org.sakaiproject.search.util.FileUtils;

import junit.framework.TestCase;

/**
 * @author ieb
 */
public class LoadSaveSegmentListTest extends TestCase
{

	private File testBase;
	private JournaledFSIndexStorage journaledFSIndexStorage;

	/**
	 * @param name
	 */
	public LoadSaveSegmentListTest(String name)
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
		journaledFSIndexStorage = new JournaledFSIndexStorage();
		testBase = new File("m2-target");
		testBase = new File(testBase, "LoadSaveSegmentListTest");
		FileUtils.deleteAll(testBase);
		
		journaledFSIndexStorage.setSearchIndexDirectory(testBase.getAbsolutePath());
		journaledFSIndexStorage.setServerConfigurationService(new MockServerConfigurationService());
		

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
	 * {@link org.sakaiproject.search.journal.impl.JournaledFSIndexStorage#init()}.
	 * @throws IOException 
	 */
	public final void testInitLoad() throws IOException
	{
		FileUtils.deleteAll(testBase);
		testBase.mkdirs();

		List<File> tl = new ArrayList<File>();
		for ( int i = 0; i < 100; i++ ) {
			tl.add(new File(testBase,String.valueOf(i)));
		}
		journaledFSIndexStorage.setSegments(tl);
		journaledFSIndexStorage.saveSegmentList();
		
		journaledFSIndexStorage.init();
		
		File[] f = journaledFSIndexStorage.getSegments();
		assertEquals("Should have given empty list ",100,f.length);
		for ( int i = 0; i < 100; i++ ) {
			assertEquals("Mismatch in files ", f[i].getAbsolutePath(),tl.get(i).getAbsolutePath());
		}

	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.journal.impl.JournaledFSIndexStorage#init()}.
	 * @throws IOException 
	 */
	public final void testInitNoLoad() throws IOException
	{
		FileUtils.deleteAll(testBase);
		testBase.mkdirs();
		
		journaledFSIndexStorage.init();
		File[] f = journaledFSIndexStorage.getSegments();
		assertEquals("Should have given empty list ",0,f.length);

	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.journal.impl.JournaledFSIndexStorage#saveSegmentList()}.
	 * @throws IOException 
	 */
	public final void testSaveSegmentList() throws IOException
	{
		FileUtils.deleteAll(testBase);
		testBase.mkdirs();
		
		journaledFSIndexStorage.setSegments(new ArrayList<File>());
		journaledFSIndexStorage.saveSegmentList();
		List<File> tl = new ArrayList<File>();
		for ( int i = 0; i < 100; i++ ) {
			tl.add(new File(testBase,String.valueOf(i)));
		}
		journaledFSIndexStorage.setSegments(tl);
		journaledFSIndexStorage.saveSegmentList();
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.search.journal.impl.JournaledFSIndexStorage#loadSegmentList()}.
	 * @throws IOException 
	 */
	public final void testLoadSegmentList() throws IOException
	{
		FileUtils.deleteAll(testBase);
		testBase.mkdirs();
		
		journaledFSIndexStorage.setSegments(new ArrayList<File>());
		journaledFSIndexStorage.saveSegmentList();
		journaledFSIndexStorage.loadSegmentList();
		File[] f = journaledFSIndexStorage.getSegments();
		assertEquals("Should have given empty list ",0,f.length);
		List<File> tl = new ArrayList<File>();
		for ( int i = 0; i < 100; i++ ) {
			tl.add(new File(testBase,String.valueOf(i)));
		}
		journaledFSIndexStorage.setSegments(tl);
		journaledFSIndexStorage.saveSegmentList();
		journaledFSIndexStorage.loadSegmentList();
		f = journaledFSIndexStorage.getSegments();
		assertEquals("Should have given empty list ",100,f.length);
		for ( int i = 0; i < 100; i++ ) {
			assertEquals("Mismatch in files ", f[i].getAbsolutePath(),tl.get(i).getAbsolutePath());
		}
	}

}
