/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.importer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportFileParser;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.FileResource;
import org.sakaiproject.importer.impl.importables.Folder;

import junit.framework.TestCase;

public class SakaiArchiveTest extends TestCase {
	private static ImportFileParser parser;
	private byte[] archiveData;
	private FileInputStream archiveStream;
	
	public void setUp() throws IOException {
		System.out.println("doing setUp()");
		parser = new SakaiArchiveFileParser();
		archiveStream = new FileInputStream(new File("/Users/zach/Downloads/sakai_course_export.zip"));
		archiveData = new byte[archiveStream.available()];
		archiveStream.read(archiveData,0,archiveStream.available());
		archiveStream.close();
	}
	public void testCanGetDataSource() {
		ImportDataSource dataSource = (ImportDataSource) parser.parse(archiveData, "/Users/zach/Desktop");
		assertNotNull(dataSource);
		System.out.println("There are " + dataSource.getItemCategories().size() + " categories in this archive.");
		((SakaiArchiveDataSource)dataSource).buildSourceFolder(dataSource.getItemCategories());
	}
	
	public void testArchiveIsValid() {
		assertTrue(parser.isValidArchive(archiveData));
	}
	

}
