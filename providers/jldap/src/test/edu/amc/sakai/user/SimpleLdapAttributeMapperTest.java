/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 Sakai Foundation
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

package edu.amc.sakai.user;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.user.api.UserEdit;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;

import junit.framework.TestCase;

public class SimpleLdapAttributeMapperTest extends TestCase {
	
	private SimpleLdapAttributeMapper attributeMapper;

	protected void setUp() {
		attributeMapper = new SimpleLdapAttributeMapper();
	}
	
	public void testInitializationCachesDefaultMappingsIfNoMappingsExplicitlySpecified() {
		assertNull(attributeMapper.getAttributeMappings()); // sanity check
		attributeMapper.init(); // the code exercise
		assertHasDefaultMappings();
	}
	
	public void testExplicitlySpecifiedMappingsCompletelyReplaceDefaults() {
		Map<String,String> expectedMappings = new HashMap<String,String>();
		expectedMappings.put("expected-key", "expected-value");
		attributeMapper.setAttributeMappings(expectedMappings); // the code exercise
		attributeMapper.init(); // satisfy the contract
		assertEquals(expectedMappings, attributeMapper.getAttributeMappings());
		assertEquals(attributeMapper.reverseAttributeMap(expectedMappings),
				attributeMapper.getReverseAttributeMap());
	}
	
	public void testExplicitlySpecifyingNullMappingsRevertsToDefaults() {
		attributeMapper.setAttributeMappings(null); // the code exercise
		attributeMapper.init(); // satisfy the contract
		assertHasDefaultMappings();
	}
	
	public void testExplicitlySpecifyingEmptyMappingsRevertsToDefaults() {
		Map<String,String> emptyMap = new HashMap<String,String>();
		attributeMapper.setAttributeMappings(emptyMap); // the code exercise
		attributeMapper.init(); // satisfy the contract
		assertHasDefaultMappings();
	}
	
	protected void assertHasDefaultMappings() {
		assertEquals(AttributeMappingConstants.DEFAULT_ATTR_MAPPINGS,
				attributeMapper.getAttributeMappings());
		assertEquals(attributeMapper.reverseAttributeMap(AttributeMappingConstants.DEFAULT_ATTR_MAPPINGS),
				attributeMapper.getReverseAttributeMap());
	}
	
	public void testExtraAttributeMapping() {
		attributeMapper.setAttributeMappings(null);
		attributeMapper.init();
		
		//Checking to see that the attribute mapper doesn't blow up when it gets back extra attributes -SAK-14632
		LDAPAttributeSet attributes = new LDAPAttributeSet();
		attributes.add(new LDAPAttribute(AttributeMappingConstants.DEFAULT_EMAIL_ATTR, "email@example.com"));
		attributes.add(new LDAPAttribute("unrequestedAttribute", "someValue"));
		LDAPEntry ldapEntry = new LDAPEntry("somestring",attributes);
		LdapUserData userData = new LdapUserData();
		
		attributeMapper.mapLdapEntryOntoUserData(ldapEntry, userData);
		
		assertEquals("email@example.com", userData.getEmail());
		
	}

}
