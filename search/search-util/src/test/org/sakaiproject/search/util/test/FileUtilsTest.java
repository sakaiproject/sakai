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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Random;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.search.util.FileUtils;

/**
 * @author ieb
 *
 */
@Slf4j
public class FileUtilsTest extends TestCase
{

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
		testSpace = new File("target");
		testSpace = new File(testSpace,"FileUtilsTestDir");
		testSpace = new File(testSpace,"work");
		if (!testSpace.exists()) {
			if (!testSpace.mkdirs()) {
				log.warn("setup: failed to create directory");
			}
		}
		
		super.setUp();
		
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
		//testSpace.delete();
		//testSpace.getParentFile().delete();
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
		Random random = new Random();
		byte[] buffer = new byte[1024];
		for ( int i = 0; i < 20; i++ ) {
			String name = FileUtils.digest(String.valueOf(System.currentTimeMillis()+i));
			File f = base;
			for ( int j = 0; j < name.length(); j++ ) {
				f = new File(f,String.valueOf(name.charAt(j)));
			}
			if (!f.getParentFile().mkdirs()){
				log.warn("createFiles: couldn't create parent files");
			}
			FileOutputStream fw = new FileOutputStream(f);
			random.nextBytes(buffer);
			fw.write(buffer);
			fw.close();
			
		}
		assertEquals("Failed to create test tree ",true,base.exists());

	}
	private void createFlatFiles(File base) throws GeneralSecurityException, IOException
	{
		Random random = new Random();
		byte[] buffer = new byte[1024];
		log.info("Create Test Tree "+base.getAbsolutePath());
		for ( int i = 0; i < 20; i++ ) {
			String name = FileUtils.digest(String.valueOf(System.currentTimeMillis()+i));
			File f = new File(base,name);
			if (!f.getParentFile().exists()) {
				if (!f.getParentFile().mkdirs()) {
					log.warn("createFlatFiles: can't create parent folder  for " + f.getParentFile().getPath());
				}
			}
			FileOutputStream fw = new FileOutputStream(f);
			random.nextBytes(buffer);
			fw.write(buffer);
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
		FileUtils.deleteAll(target);
		FileUtils.deleteAll(zipFile);
		createFiles(target);
		log.info("Packing From "+target.getAbsolutePath());
		log.info("Packing Into "+zipFile.getAbsolutePath());
		String basePath = target.getPath();
		String replacePath = "somethingelse/";
		
		FileOutputStream fout = new FileOutputStream(zipFile);
		FileUtils.pack(target, basePath, replacePath, fout, true);
		fout.close();
		
		assertEquals("Packed File Does not exist",true, zipFile.exists());
		assertEquals("File Size == 0 ",true,zipFile.length() > 0);
		//FileUtils.deleteAll(target);
		//FileUtils.deleteAll(zipFile);
		//assertEquals("Delete Failed of source tree ",false,target.exists());
		//assertEquals("Delete Failed of zip file ",false,zipFile.exists());
		log.info("Test Pack Ok  ");
	}
	public final void testPackUncompressed() throws Exception
	{
		File target = new File(testSpace,"testPack");
		File zipFile = new File(target.getParentFile(),"testPack.zip");
		FileUtils.deleteAll(target);
		FileUtils.deleteAll(zipFile);
		createFiles(target);
		log.info("Packing From "+target.getAbsolutePath());
		log.info("Packing Into "+zipFile.getAbsolutePath());
		String basePath = target.getPath();
		String replacePath = "somethingelse/";
		
		FileOutputStream fout = new FileOutputStream(zipFile);
		FileUtils.pack(target, basePath, replacePath, fout, false);
		fout.close();
		
		assertEquals("Packed File Does not exist",true, zipFile.exists());
		assertEquals("File Size == 0 ",true,zipFile.length() > 0);
		//FileUtils.deleteAll(target);
		//FileUtils.deleteAll(zipFile);
		//assertEquals("Delete Failed of source tree ",false,target.exists());
		//assertEquals("Delete Failed of zip file ",false,zipFile.exists());
		log.info("Test Pack Ok  ");
	}
	public final void testPackFlat() throws Exception
	{
		File target = new File(testSpace,"testPackFlat");
		File zipFile = new File(target.getParentFile(),"testPackFlat.zip");
		FileUtils.deleteAll(target);
		FileUtils.deleteAll(zipFile);
		createFlatFiles(target);
		log.info("Packing From "+target.getAbsolutePath());
		log.info("Packing Into "+zipFile.getAbsolutePath());
		String basePath = target.getPath();
		String replacePath = "somethingelse";
		
		FileOutputStream fout = new FileOutputStream(zipFile);
		FileUtils.pack(target, basePath, replacePath, fout, true);
		fout.close();
		
		assertEquals("Packed File Does not exist",true, zipFile.exists());
		assertEquals("File Size == 0 ",true,zipFile.length() > 0);
		//FileUtils.deleteAll(target);
		//FileUtils.deleteAll(zipFile);
		//assertEquals("Delete Failed of source tree ",false,target.exists());
		//assertEquals("Delete Failed of zip file ",false,zipFile.exists());
		log.info("Test Pack Ok  ");
	}
	public final void testPackFlatUncompressed() throws Exception
	{
		File target = new File(testSpace,"testPackFlat");
		File zipFile = new File(target.getParentFile(),"testPackFlat.zip");
		FileUtils.deleteAll(target);
		FileUtils.deleteAll(zipFile);
		createFlatFiles(target);
		log.info("Packing From "+target.getAbsolutePath());
		log.info("Packing Into "+zipFile.getAbsolutePath());
		String basePath = target.getPath();
		String replacePath = "somethingelse";
		
		FileOutputStream fout = new FileOutputStream(zipFile);
		FileUtils.pack(target, basePath, replacePath, fout, false);
		fout.close();
		
		assertEquals("Packed File Does not exist",true, zipFile.exists());
		assertEquals("File Size == 0 ",true,zipFile.length() > 0);
		//FileUtils.deleteAll(target);
		//FileUtils.deleteAll(zipFile);
		//assertEquals("Delete Failed of source tree ",false,target.exists());
		//assertEquals("Delete Failed of zip file ",false,zipFile.exists());
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
		log.info("Packing From "+target.getAbsolutePath());
		log.info("Packing Into "+zipFile.getAbsolutePath());
		String basePath = target.getPath();
		String replacePath = "somethingelse";
		
		FileOutputStream fout = new FileOutputStream(zipFile);
		FileUtils.pack(target, basePath, replacePath, fout,true);
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
	public final void testUnpackUncompressed() throws Exception
	{
		File target = new File(testSpace,"testUnpack");
		File outtarget = new File(testSpace,"testUnpackOut");
		File zipFile = new File(target.getParentFile(),"testUnpack.zip");
		createFiles(target);
		log.info("Packing From "+target.getAbsolutePath());
		log.info("Packing Into "+zipFile.getAbsolutePath());
		String basePath = target.getPath();
		String replacePath = "somethingelse";
		
		FileOutputStream fout = new FileOutputStream(zipFile);
		FileUtils.pack(target, basePath, replacePath, fout,false);
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
