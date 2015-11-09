/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.content.metadata.logic;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.sakaiproject.content.metadata.model.MetadataType;


public class JsonMetadataParserTest {

	private MetadataParser parser;

	@Before
	public void setUp() throws Exception {
		parser = new JsonMetadataParser();
	}

	@Test
	public void testGoodParse() {
		InputStream in = getClass().getResourceAsStream("/simple-metadata.json");
		assertNotNull(in);
		List<MetadataType> parse = parser.parse(in);
		assertNotNull(parse);
		assertTrue(parse.size() > 0);
	}

}
