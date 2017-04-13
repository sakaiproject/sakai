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

import java.util.List;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;
import junit.framework.TestCase;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static edu.amc.sakai.user.AttributeMappingConstants.DEFAULT_EMAIL_ATTR;
import static edu.amc.sakai.user.AttributeMappingConstants.DEFAULT_LOGIN_ATTR;
import static edu.amc.sakai.user.AttributeMappingConstants.LOGIN_ATTR_MAPPING_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
	
	public void testSkipsExtraAttributeMapping() {
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
	
	public void testMultipleValues() {
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("ou", "ou");
		attributeMapper.setAttributeMappings(mappings);
		attributeMapper.init();
		
		LDAPAttributeSet attributes = new LDAPAttributeSet();
		attributes.add(new LDAPAttribute("ou", new String[]{"Unit 1", "Unit 2"}));
		LDAPEntry ldapEntry = new LDAPEntry("somestring", attributes);
		LdapUserData userData = new LdapUserData();
		
		attributeMapper.mapLdapEntryOntoUserData(ldapEntry, userData);
		assertTrue(userData.getProperties().get("ou") instanceof List);
		
	}

	public void testValueMapping() {
		attributeMapper.setValueMappings(Collections.singletonMap(LOGIN_ATTR_MAPPING_KEY,
				new MessageFormat("{0}@EXAMPLE.COM")));
		attributeMapper.init();
		LDAPAttributeSet attributes = new LDAPAttributeSet();
		// Example Kerberos principal (and we want to remove the domain
		attributes.add(new LDAPAttribute(DEFAULT_LOGIN_ATTR, "user@EXAMPLE.COM"));
		attributes.add(new LDAPAttribute(DEFAULT_EMAIL_ATTR, "user@example.com"));
		LDAPEntry ldapEntry = new LDAPEntry("id=user,dc=example,dc=com", attributes);
		LdapUserData userData = new LdapUserData();
		attributeMapper.mapLdapEntryOntoUserData(ldapEntry, userData);
		assertEquals("user", userData.getEid()); // Check the domain got removed.
		assertEquals("user@example.com", userData.getEmail());
	}

	public void testValueMappingChecking() {
		// This checks that we filter out bad value mappings.
		// Have to use a proper map as we need to remove an item from it.
		Map<String, MessageFormat> valueMappings = new HashMap<String, MessageFormat>();
		valueMappings.put(LOGIN_ATTR_MAPPING_KEY, new MessageFormat("{0}to{1}many{2}"));
		attributeMapper.setValueMappings(valueMappings);
		attributeMapper.init();
		assertEquals("We should have removed the bad format.", 0, attributeMapper.getValueMappings().size());
	}



}
