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
 *       http://www.opensource.org/licenses/ECL-2.0
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

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportFileParser;

@Slf4j
public class SakaiArchiveTest {
	private static ImportFileParser parser = null;
	private FileInputStream archiveStream = null;

	@Before
	public void setUp() throws IOException {
		parser = new SakaiArchiveFileParser();
		archiveStream = new FileInputStream(new File("sakai_course_export.zip"));
	}

	@Ignore
	@Test
	public void testCanGetDataSource() {
		ImportDataSource dataSource = (ImportDataSource) parser.parse(archiveStream, "Desktop");
		Assert.assertNotNull(dataSource);
		log.debug("There are {} categories in this archive.", dataSource.getItemCategories().size());
		((SakaiArchiveDataSource)dataSource).buildSourceFolder(dataSource.getItemCategories());
	}

	@Ignore
	@Test
	public void testArchiveIsValid() {
		Assert.assertTrue(parser.isValidArchive(archiveStream));
	}
	

}
