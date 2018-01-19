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
import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportFileParser;
import org.sakaiproject.importer.impl.importables.Assessment;
import org.sakaiproject.importer.impl.importables.HtmlDocument;

@Slf4j
public class CommonCartridgeTest {
	private static ImportFileParser parser = null;
	private InputStream archiveStream = null;

	@Before
	public void setUp() {
		try {
			parser = new CommonCartridgeFileParser();
			FileInputStream archiveStream = new FileInputStream(new File("psychology.zip"));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Ignore
	@Test
	public void testIsValidArchive() {
		Assert.assertTrue(parser.isValidArchive(archiveStream));
	}

	@Ignore
	@Test
	public void testCanGetQti() {
		ImportDataSource ids = parser.parse(archiveStream, "psychology");
		Collection importables = ids.getItemsForCategories(ids.getItemCategories());
		int numberOfAssessments = 0;
		int numberOfWebContent = 0;
		for (Iterator i = importables.iterator();i.hasNext();) {
			Object x = i.next();
			if(x instanceof Assessment) {
				numberOfAssessments++;
			} else if(x instanceof HtmlDocument) {
				numberOfWebContent++;
			}
		}
		log.debug("{} top-level items", ids.getItemCategories().size());
		log.debug("{} importables", importables.size());
		log.debug("{} assessments", numberOfAssessments);
		log.debug("{} webcontent", numberOfWebContent);
		Assert.assertTrue("Why no assessments?", numberOfAssessments > 0);
	}

}
