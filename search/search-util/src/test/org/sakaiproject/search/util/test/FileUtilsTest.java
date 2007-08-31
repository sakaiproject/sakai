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

package org.sakaiproject.search.util.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.util.FileUtils;

/**
 * @author ieb
 *
 */
public class FileUtilsTest extends TestCase
{

	private static final Log log = LogFactory.getLog(FileUtilsTest.class);
	private File testSpace;

	/**
	 * @param name
	 */
	public FileUtilsTest(String name)
	{
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		testSpace = new File("m2-target");
		testSpace = new File(testSpace,"FileUtilsTestDir");
		testSpace = new File(testSpace,"work");
		testSpace.mkdirs();
		
		super.setUp();
		
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
		testSpace.delete();
		testSpace.getParentFile().delete();
	}

	/**
	 * Test method for {@link org.sakaiproject.search.util.FileUtils#deleteAll(java.io.File)}.
	 * @throws IOException 
	 * @throws Exception 
	 */
	public final void testDeleteAll() throws Exception
	{
	
		File target = new File(testSpace,"testDeleteAll");
		createFiles(target);
		FileUtils.deleteAll(target);
		assertEquals("Delete Failed ",false,target.exists());
		log.info("Delete Ok On  "+target.getAbsolutePath());
	}
	
	public final void testListAll() throws Exception 
	{
		File target = new File(testSpace,"testListAll");
		createFiles(target);
		FileUtils.listDirectory(target);
		FileUtils.deleteAll(target);
		assertEquals("Delete Failed ",false,target.exists());
		log.info("Delete Ok On  "+target.getAbsolutePath());

	}

	/**
	 * @param testSpace2
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 */
	private void createFiles(File base) throws GeneralSecurityException, IOException
	{
		log.info("Create Test Tree "+base.getAbsolutePath());
		for ( int i = 0; i < 20; i++ ) {
			String name = FileUtils.digest(String.valueOf(System.currentTimeMillis()+i));
			File f = base;
			for ( int j = 0; j < name.length(); j++ ) {
				f = new File(f,String.valueOf(name.charAt(j)));
			}
			f.getParentFile().mkdirs();
			FileWriter fw = new FileWriter(f);
			fw.write("TESTDATASPACE OK to delete");
			fw.close();
			
		}
		assertEquals("Failed to create test tree ",true,base.exists());

	}

	/**
	 * Test method for {@link org.sakaiproject.search.util.FileUtils#pack(java.io.File, java.io.OutputStream)}.
	 * @throws Exception 
	 */
	public final void testPack() throws Exception
	{
		File target = new File(testSpace,"testPack");
		File zipFile = new File(target.getParentFile(),"testPack.zip");
		createFiles(target);
		FileOutputStream fout = new FileOutputStream(zipFile);
		log.info("Packing From "+target.getAbsolutePath());
		log.info("Packing Into "+zipFile.getAbsolutePath());
		String basePath = target.getPath();
		String replacePath = "somethingelse/";
		FileUtils.pack(target, basePath, replacePath, fout);
		fout.close();
		assertEquals("Packed File Does not exist",true, zipFile.exists());
		assertEquals("File Size == 0 ",true,zipFile.length() > 0);
		FileUtils.deleteAll(target);
		FileUtils.deleteAll(zipFile);
		assertEquals("Delete Failed of source tree ",false,target.exists());
		assertEquals("Delete Failed of zip file ",false,zipFile.exists());
		log.info("Test Pack Ok  ");
	}

	/**
	 * Test method for {@link org.sakaiproject.search.util.FileUtils#unpack(java.io.InputStream, java.io.File)}.
	 * @throws Exception 
	 */
	public final void testUnpack() throws Exception
	{
		File target = new File(testSpace,"testUnpack");
		File outtarget = new File(testSpace,"testUnpackOut");
		File zipFile = new File(target.getParentFile(),"testUnpack.zip");
		createFiles(target);
		FileOutputStream fout = new FileOutputStream(zipFile);
		log.info("Packing From "+target.getAbsolutePath());
		log.info("Packing Into "+zipFile.getAbsolutePath());
		String basePath = target.getPath();
		String replacePath = "somethingelse/";
		FileUtils.pack(target, basePath, replacePath, fout);
		fout.close();
		assertEquals("Packed File Does not exist",true, zipFile.exists());
		assertEquals("File Size == 0 ",true,zipFile.length() > 0);
		
		FileInputStream fin = new FileInputStream(zipFile);
		log.info("UnPacking From "+zipFile.getAbsolutePath());
		log.info("UnPacking Into "+outtarget.getAbsolutePath());
		FileUtils.unpack(fin, outtarget);
		fin.close();
		assertEquals("unpack the tree ok ",true,outtarget.exists());
	
		FileUtils.deleteAll(target);
		FileUtils.deleteAll(outtarget);
		FileUtils.deleteAll(zipFile);
		assertEquals("Delete Failed of source tree ",false,target.exists());
		assertEquals("Delete Failed of unpacked tree ",false,outtarget.exists());
		assertEquals("Delete Failed of zip file ",false,zipFile.exists());
		log.info("Test UnPack Ok  ");
	}

}
