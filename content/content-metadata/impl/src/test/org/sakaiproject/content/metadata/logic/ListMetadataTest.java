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

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.sakaiproject.content.metadata.model.ListMetadataType;
import org.sakaiproject.content.metadata.model.MetadataConverter;
import org.sakaiproject.content.metadata.model.MetadataType;
import org.sakaiproject.content.metadata.model.StringMetadataType;

public class ListMetadataTest {

	private ListMetadataType<String> type;
	
	@Before
	public void setUp() throws Exception {
		MetadataType<String> metadata = new StringMetadataType();
		metadata.setName("Test");
		metadata.setUniqueName("test");
		type = new ListMetadataType<String>(metadata);
	}

	@Test
	public void testFromString() {
		MetadataConverter<List<String>> converter = type.getConverter();
		assertEquals(Collections.emptyList(), converter.fromString(null));
		assertEquals(Collections.singletonList("value"), converter.fromString("[\"value\"]"));
		assertEquals(Collections.emptyList(), converter.fromString(""));
	}
	
	@Test
	public void testFromProperties() {
		MetadataConverter<List<String>> converter = type.getConverter();
		assertEquals(Collections.emptyList(), converter.fromProperties(Collections.<String,Object>emptyMap()));
		assertEquals(Collections.emptyList(), converter.fromProperties(Collections.singletonMap("test", null)));
		// This is WL-2461.
		assertEquals(Collections.emptyList(), converter.fromProperties(Collections.singletonMap("test", "")));
		// It might be better to be relaxed about this and return a list containing value.
		assertEquals(Collections.emptyList(), converter.fromProperties(Collections.singletonMap("test", "value")));
		assertEquals(Collections.singletonList("value"), converter.fromProperties(Collections.singletonMap("test", Collections.singletonList("value"))));
		// Wrong key in the map.
		assertEquals(Collections.emptyList(), converter.fromProperties(Collections.singletonMap("other", Collections.singletonList("value"))));
	}

}
