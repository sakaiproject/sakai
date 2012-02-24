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

package edu.amc.sakai.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class MultipleEmailLdapAttributeMapperTest extends TestCase {

	private MultipleEmailLdapAttributeMapper mapper;
	
	protected void setUp() {
		mapper = new MultipleEmailLdapAttributeMapper();
	}
	
	public void testGeneratesMultiTermSearchFilter() {
		
		final String LOGICAL_ATTR_NAME_1 = AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY + "1";
		final String LOGICAL_ATTR_NAME_2 = AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY + "2";
		final String LOGICAL_ATTR_NAME_3 = AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY + "3";
		final String PHSYICAL_ATTR_NAME_1 = AttributeMappingConstants.DEFAULT_EMAIL_ATTR + "1";
		final String PHSYICAL_ATTR_NAME_2 = AttributeMappingConstants.DEFAULT_EMAIL_ATTR + "2";
		final String PHSYICAL_ATTR_NAME_3 = AttributeMappingConstants.DEFAULT_EMAIL_ATTR + "3";
		
		Map<String,String> attributeMappings = new HashMap<String,String>();
		attributeMappings.put(LOGICAL_ATTR_NAME_1,PHSYICAL_ATTR_NAME_1);
		attributeMappings.put(LOGICAL_ATTR_NAME_2,PHSYICAL_ATTR_NAME_2);
		attributeMappings.put(LOGICAL_ATTR_NAME_3,PHSYICAL_ATTR_NAME_3);
		
		List<String> emailAttributeNames = new ArrayList<String>();
		emailAttributeNames.add(LOGICAL_ATTR_NAME_1);
		emailAttributeNames.add(LOGICAL_ATTR_NAME_2);
		emailAttributeNames.add(LOGICAL_ATTR_NAME_3);
		
		mapper.setAttributeMappings(attributeMappings);
		mapper.setSearchableEmailAttributes(emailAttributeNames);
		mapper.init();
		
		final String SEARCH_TERM = "some-email@university.edu";
		final String expectedFilter = 
			"(|(|(" + PHSYICAL_ATTR_NAME_1 + "=" + SEARCH_TERM + ")(" + 
				PHSYICAL_ATTR_NAME_2 + "=" + SEARCH_TERM + "))(" +
				PHSYICAL_ATTR_NAME_3 + "=" + SEARCH_TERM + "))";
		assertEquals(expectedFilter, mapper.getFindUserByEmailFilter(SEARCH_TERM));
		
	}
	
	public void testSkipsUnmappedLogicalEmailAttributeNames() {
		final String LOGICAL_ATTR_NAME_1 = AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY + "1";
		final String LOGICAL_ATTR_NAME_2 = AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY + "2";
		final String LOGICAL_ATTR_NAME_3 = AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY + "3";
		final String PHSYICAL_ATTR_NAME_1 = AttributeMappingConstants.DEFAULT_EMAIL_ATTR + "1";
		final String PHSYICAL_ATTR_NAME_2 = AttributeMappingConstants.DEFAULT_EMAIL_ATTR + "2";
		final String PHSYICAL_ATTR_NAME_3 = AttributeMappingConstants.DEFAULT_EMAIL_ATTR + "3";
		
		Map<String,String> attributeMappings = new HashMap<String,String>();
		attributeMappings.put(LOGICAL_ATTR_NAME_1,PHSYICAL_ATTR_NAME_1);
		// leave out LOGICAL_ATTR_NAME_2
		attributeMappings.put(LOGICAL_ATTR_NAME_3,PHSYICAL_ATTR_NAME_3);
		
		List<String> emailAttributeNames = new ArrayList<String>();
		emailAttributeNames.add(LOGICAL_ATTR_NAME_1);
		emailAttributeNames.add(LOGICAL_ATTR_NAME_2);
		emailAttributeNames.add(LOGICAL_ATTR_NAME_3);
		
		mapper.setAttributeMappings(attributeMappings);
		mapper.setSearchableEmailAttributes(emailAttributeNames);
		mapper.init();
		
		final String SEARCH_TERM = "some-email@university.edu";
		final String expectedFilter = 
			"(|(" + PHSYICAL_ATTR_NAME_1 + "=" + SEARCH_TERM + ")(" +
				PHSYICAL_ATTR_NAME_3 + "=" + SEARCH_TERM + "))";
		assertEquals(expectedFilter, mapper.getFindUserByEmailFilter(SEARCH_TERM));
	}
	
	public void testGeneratesDefaultSearchFilterIfNoMappedLogicalEmailAttributeNames() {
		final String LOGICAL_ATTR_NAME_1 = AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY + "1";
		final String LOGICAL_ATTR_NAME_2 = AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY + "2";
		final String LOGICAL_ATTR_NAME_3 = AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY + "3";
		
		// none of these will exist in the attrib name map
		List<String> emailAttributeNames = new ArrayList<String>();
		emailAttributeNames.add(LOGICAL_ATTR_NAME_1);
		emailAttributeNames.add(LOGICAL_ATTR_NAME_2);
		emailAttributeNames.add(LOGICAL_ATTR_NAME_3);
		
		final String SEARCH_TERM = "some-email@university.edu";
		mapper.setSearchableEmailAttributes(emailAttributeNames);
		mapper.init();
		SimpleLdapAttributeMapper parentMapper = new SimpleLdapAttributeMapper();
		parentMapper.init();
		final String expectedFilter = parentMapper.getFindUserByEmailFilter(SEARCH_TERM);
		
		assertEquals(expectedFilter, mapper.getFindUserByEmailFilter(SEARCH_TERM));
	}
	
	public void testGeneratesDefaultSearchFilterIfNoEmailAttributeListExplicitlySpecified() {
		final String SEARCH_TERM = "some-email@university.edu";
		mapper.init(); // should have an empty list of searchable email addr attribs
		SimpleLdapAttributeMapper parentMapper = new SimpleLdapAttributeMapper();
		parentMapper.init();
		final String expectedFilter = parentMapper.getFindUserByEmailFilter(SEARCH_TERM);
		assertEquals(expectedFilter, mapper.getFindUserByEmailFilter(SEARCH_TERM));
	}
	
	public void testTreatsNullEmailAttributeListAsEmptyList() {
		final String SEARCH_TERM = "some-email@university.edu";
		mapper.setSearchableEmailAttributes(null); // explicitly set to null
		mapper.init(); // should have an empty list of searchable email addr attribs
		SimpleLdapAttributeMapper parentMapper = new SimpleLdapAttributeMapper();
		parentMapper.init();
		final String expectedFilter = parentMapper.getFindUserByEmailFilter(SEARCH_TERM);
		assertEquals(expectedFilter, mapper.getFindUserByEmailFilter(SEARCH_TERM));
	}
	
	public void testFiltersSearchTerm() {
		final String PHYS_ATTR_NAME = "some-attr";
		final String SEARCH_TERM = "*@university.edu"; // * should be scrubbed
		final String SCRUBBED_SEACRH_TERM = "\\2a@university.edu";
		StringBuilder generatedFilter = new StringBuilder();
		mapper.appendSingleSearchPredicate(generatedFilter, PHYS_ATTR_NAME, SEARCH_TERM);
		final String expectedFilter = PHYS_ATTR_NAME + "=" + SCRUBBED_SEACRH_TERM;
		assertEquals(expectedFilter, generatedFilter.toString());
	}
	
}
